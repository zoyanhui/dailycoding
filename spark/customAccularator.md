# Custom Accumulator in Spark 2.1
Accumulator can sum or count number in spark tasks over all nodes, and then return the final result. For example, LongAccumulator.
But when I want to accumulate value by custom way, it can be implemented by extends AccumulatorV2 in spark 2.1.

Below, a MapLongAccumulator is implemented to count the numbers of several values seperately.

```
import java.util
import java.util.Collections

import org.apache.spark.util.AccumulatorV2
import scala.collection.JavaConversions._


class MapLongAccumulator[T] extends AccumulatorV2[T, java.util.Map[T, java.lang.Long]] {
    private val _map: java.util.Map[T, java.lang.Long] = Collections.synchronizedMap(new util.HashMap[T, java.lang.Long]())

    override def isZero: Boolean = _map.isEmpty

    override def copyAndReset(): MapLongAccumulator[T] = new MapLongAccumulator

    override def copy(): MapLongAccumulator[T] = {
        val newAcc = new MapLongAccumulator[T]
        _map.synchronized {
            newAcc._map.putAll(_map)
        }
        newAcc
    }

    override def reset(): Unit = _map.clear()

    override def add(v: T): Unit = _map.synchronized {
        val old = _map.put(v, 1l)
        if (old != null) {
            _map.put(v, 1 + old)
        }
    }

    override def merge(other: AccumulatorV2[T, java.util.Map[T, java.lang.Long]]): Unit = other match {
        case o: MapLongAccumulator[T] => {
            for ((k,v) <- o.value) {
                val old = _map.put(k, v)
                if(old != null){
                    _map.put(k, old.longValue() + v)
                }
            }
        }
        case _ => throw new UnsupportedOperationException(
            s"Cannot merge ${this.getClass.getName} with ${other.getClass.getName}")
    }

    override def value: java.util.Map[T, java.lang.Long] = _map.synchronized {
        java.util.Collections.unmodifiableMap(new util.HashMap[T, java.lang.Long](_map))
    }

    def setValue(newValue: java.util.Map[T, java.lang.Long]): Unit = {
        _map.clear()
        _map.putAll(newValue)
    }
}
```

Use case:
```
val accumulator = new MapLongAccumulator[String]()
sc.register(accumulator, "CustomAccumulator")
someRdd.map(a => {
            ...
            val convertResult = convert(a)
            convertResultAccumulator.add(convertResult.toString)
            ...
        }).repartition(1).saveAsTextFile(outputPath)
System.out.println(accumulator.value)   // the several convertResults will be counted seperately, and then get the output value.
```

AccumulatorV2 is new `accumulator` class since spark 2.0.0.

```
public abstract class AccumulatorV2<IN,OUT>
extends Object
implements scala.Serializable
```

>The base class for accumulators, that can accumulate inputs of type IN, and produce output of type OUT.
*OUT should be a type that can be read atomically (e.g., Int, Long), or thread-safely (e.g., synchronized collections) because it will be read from other threads*.

Methods of AccumulatorV2:
`merge` method doesn't need thread-safe, it is called only in one thread when task completion.(`handleTaskCompletion` in `DAGScheduler`)
``` Java
org.apache.spark.scheduler.DAGScheduler.scala
/**
   * Responds to a task finishing. This is called inside the event loop so it assumes that it can
   * modify the scheduler's internal state. Use taskEnded() to post a task end event from outside.
   */
  private[scheduler] def handleTaskCompletion(event: CompletionEvent) {
    val task = event.task
    val taskId = event.taskInfo.id
    val stageId = task.stageId
    val taskType = Utils.getFormattedClassName(task)

    outputCommitCoordinator.taskCompleted(
      stageId,
      task.partitionId,
      event.taskInfo.attemptNumber, // this is a task attempt number
      event.reason)

    // Reconstruct task metrics. Note: this may be null if the task has failed.
    val taskMetrics: TaskMetrics =
      if (event.accumUpdates.nonEmpty) {
        try {
          TaskMetrics.fromAccumulators(event.accumUpdates)
        } catch {
          case NonFatal(e) =>
            logError(s"Error when attempting to reconstruct metrics for task $taskId", e)
            null
        }
      } else {
        null
      }

    ......
    val stage = stageIdToStage(task.stageId)
    event.reason match {
      case Success =>
        stage.pendingPartitions -= task.partitionId
        task match {
          case rt: ResultTask[_, _] =>
            // Cast to ResultStage here because it's part of the ResultTask
            // TODO Refactor this out to a function that accepts a ResultStage
            val resultStage = stage.asInstanceOf[ResultStage]
            resultStage.activeJob match {
              case Some(job) =>
                if (!job.finished(rt.outputId)) {
                  updateAccumulators(event)
                  ......
           case smt: ShuffleMapTask =>
            val shuffleStage = stage.asInstanceOf[ShuffleMapStage]
            updateAccumulators(event)
            val status = event.result.asInstanceOf[MapStatus]
            val execId = status.location.executorId
            logDebug("ShuffleMapTask finished on " + execId)
            if (failedEpoch.contains(execId) && smt.epoch <= failedEpoch(execId)) {
              logInfo(s"Ignoring possibly bogus $smt completion from executor $execId")
            } else {
              shuffleStage.addOutputLoc(smt.partitionId, status)
            }
            ......
    case exceptionFailure: ExceptionFailure =>
        // Tasks failed with exceptions might still have accumulator updates.
        updateAccumulators(event)
    ......
}


/**
   * Merge local values from a task into the corresponding accumulators previously registered
   * here on the driver.
   *
   * Although accumulators themselves are not thread-safe, this method is called only from one
   * thread, the one that runs the scheduling loop. This means we only handle one task
   * completion event at a time so we don't need to worry about locking the accumulators.
   * This still doesn't stop the caller from updating the accumulator outside the scheduler,
   * but that's not our problem since there's nothing we can do about that.
   */
  private def updateAccumulators(event: CompletionEvent): Unit = {
    val task = event.task
    val stage = stageIdToStage(task.stageId)
    try {
      event.accumUpdates.foreach { updates =>
        val id = updates.id
        // Find the corresponding accumulator on the driver and update it
        val acc: AccumulatorV2[Any, Any] = AccumulatorContext.get(id) match {
          case Some(accum) => accum.asInstanceOf[AccumulatorV2[Any, Any]]
          case None =>
            throw new SparkException(s"attempted to access non-existent accumulator $id")
        }
        acc.merge(updates.asInstanceOf[AccumulatorV2[Any, Any]])
        // To avoid UI cruft, ignore cases where value wasn't updated
        if (acc.name.isDefined && !updates.isZero) {
          stage.latestInfo.accumulables(id) = acc.toInfo(None, Some(acc.value))
          event.taskInfo.accumulables += acc.toInfo(Some(updates.value), Some(acc.value))
        }
      }
    } catch {
      case NonFatal(e) =>
        logError(s"Failed to update accumulators for task ${task.partitionId}", e)
    }
  }

```

```
org.apache.spark.executor.TaskMetrics.scala
  /**
   * Construct a [[TaskMetrics]] object from a list of accumulator updates, called on driver only.
   */
  def fromAccumulators(accums: Seq[AccumulatorV2[_, _]]): TaskMetrics = {
    val tm = new TaskMetrics
    val (internalAccums, externalAccums) =
      accums.partition(a => a.name.isDefined && tm.nameToAccums.contains(a.name.get))

    internalAccums.foreach { acc =>
      val tmAcc = tm.nameToAccums(acc.name.get).asInstanceOf[AccumulatorV2[Any, Any]]
      tmAcc.metadata = acc.metadata
      tmAcc.merge(acc.asInstanceOf[AccumulatorV2[Any, Any]])
    }

    tm.externalAccums ++= externalAccums
    tm
  }
```


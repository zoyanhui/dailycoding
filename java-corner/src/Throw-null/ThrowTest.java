import java.sql.SQLException;

class ThrowTest{
    public void doThrow() throws SQLException{
        SQLException e = null;
        throw e;
    }

    public static void main(String[] args){
        try{
            new ThrowTest().doThrow();
        }catch(SQLException e){
            System.out.println(e);
        }
    }
}
import java.io.Serializable;
import java.time.LocalDate;

public class Record implements Serializable{
    private int id; //key field
    private String name_app;
    private LocalDate date;
    private boolean status;

    public Record (int id, String name_app, LocalDate date, boolean status){
        this.id = id;
        this.name_app = name_app;
        this.date = date;
        this.status = status;
    }

    //геттеры

    public int GetId(){
        return id;
    }
    public String GetNameApp(){
        return name_app;
    }
    public LocalDate GetDate(){
        return date;
    }
    public boolean GetStatus(){
        return status;
    }

    //сеттеры

    public void SetId(int n_id){
         this.id = n_id;
    }
    public void SetNameApp(String n_name){
        this.name_app = n_name;
    }
    public void GetDate(LocalDate n_date){
        this.date = n_date;
    }
    public void GetStatus(boolean n_status){
        this.status = n_status;
    }
    @Override
    public String toString() {
        return id + "  " + name_app + "  " + date + "  " + status;
    }

}
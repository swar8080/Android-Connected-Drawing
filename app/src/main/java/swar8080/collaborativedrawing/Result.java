package swar8080.collaborativedrawing;

/**
 *
 */

public class Result<T> {

    private T obj;
    private boolean result;

    //Result was succesful
    public Result(T obj){
        this.obj = obj;
        this.result = true;
    }

    //Result may be succesful (no response required)
    public Result(boolean result){
        this.result = result;
    }

    public boolean isSuccesful(){
        return result;
    }

    public T getResult(){
        return obj;
    }
}

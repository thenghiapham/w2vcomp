package common.exception;

public class IllegalOperationException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public IllegalOperationException(String msg) {
        super(msg);
    }
}

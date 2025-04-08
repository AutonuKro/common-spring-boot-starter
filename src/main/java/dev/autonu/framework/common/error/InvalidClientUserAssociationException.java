package dev.autonu.framework.common.error;

/**
 * @author autonu2X
 */
public class InvalidClientUserAssociationException extends RuntimeException {

    public InvalidClientUserAssociationException(){
    }

    public InvalidClientUserAssociationException(String message){
        super(message);
    }
}

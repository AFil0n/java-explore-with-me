package ru.practicum.extention;

public class ConditionsNotMetException extends RuntimeException{
    public ConditionsNotMetException(String message){
        super(message);
    }
}

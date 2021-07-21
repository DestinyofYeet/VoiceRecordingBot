package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

public enum Status {
    RECORDING("[RECORDING]"), IDLE("");

    private String value;

    Status(String value){
        this.value = value;
    }

    public String getText(){
        return value;
    }


}

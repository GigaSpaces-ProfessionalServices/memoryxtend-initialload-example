package com.samples.model.v2;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import java.time.LocalDateTime;
import java.util.Objects;


@SpaceClass
public class Data implements java.io.Serializable {

    private static final long serialVersionUID = 0L;

    private Integer id;
    private String message;

    private LocalDateTime timestamp;
    private Boolean processed;

    @SpaceId(autoGenerate = false)
    @SpaceRouting
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data = (Data) o;
        return Objects.equals(getId(), data.getId()) &&
                Objects.equals(getMessage(), data.getMessage()) &&
                Objects.equals(getTimestamp(), data.getTimestamp()) &&
                Objects.equals(getProcessed(), data.getProcessed());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((timestamp == null) ? 0: timestamp.hashCode());
        result = prime * result
                + ((processed == null) ? 0 : processed.hashCode());
        return result;
    }
    @Override
    public String toString() {
        return "Data{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", processed=" + processed +
                '}';
    }
}
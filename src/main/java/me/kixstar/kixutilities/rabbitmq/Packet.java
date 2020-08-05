package me.kixstar.kixutilities.rabbitmq;

import com.rabbitmq.client.AMQP;

import java.lang.reflect.Field;

public abstract class  Packet {

    private AMQP.BasicProperties props;

    public abstract byte[] serialize();

    public abstract void deserialize(byte[] raw);

    public AMQP.BasicProperties getProperties() {
        if (props == null) this.setProperties(new AMQP.BasicProperties().builder().build());
        return this.props;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getSimpleName());
        result.append(" {");
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                field.setAccessible(true);
                result.append(field.get(this));
                field.setAccessible(false);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }


    public void setProperties(AMQP.BasicProperties props) {
        this.props = props;
    }
}

package server.api.task.order;

import com.edu.schooltask.beans.TaskOrder;

import java.util.ArrayList;
import java.util.List;

import server.api.token.BaseTokenEvent;

/**
 * Created by 夜夜通宵 on 2017/5/22.
 */

public class GetTaskOrderEvent extends BaseTokenEvent {
    List<TaskOrder> taskOrders = new ArrayList<>();

    public GetTaskOrderEvent() {
    }

    public GetTaskOrderEvent(boolean ok, int code, String error) {
        super(ok, code, error);
    }

    public GetTaskOrderEvent(boolean validate, boolean update, String token) {
        super(validate, update, token);
    }

    public List<TaskOrder> getTaskOrders() {
        return taskOrders;
    }

    public void setTaskOrders(List<TaskOrder> taskOrders) {
        this.taskOrders = taskOrders;
    }
}
package com.tuul.test.user.port;

import com.tuul.test.user.model.User;
import com.tuul.test.vehicle.model.ActiveVehicle;

public interface SaveUserPort {

    User registerUser(User user);

    void saveActiveVehicle(ActiveVehicle activeVehicle);

    void deleteActiveVehicle(ActiveVehicle activeVehicle);
}

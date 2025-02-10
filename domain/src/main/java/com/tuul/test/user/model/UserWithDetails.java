package com.tuul.test.user.model;

import com.tuul.test.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserWithDetails {
    private User user;
    private Vehicle activeVehicle;
}

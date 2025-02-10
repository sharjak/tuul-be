package com.tuul.test.vehicle.service;

import com.tuul.test.UnitTest;
import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.ActiveVehicle;
import com.tuul.test.vehicle.model.Vehicle;
import com.tuul.test.vehicle.port.FetchVehiclePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class VehicleServiceUnitTest extends UnitTest {

    private static final String TOKEN = "dummy-jwt-token";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VEHICLE_CODE = "code1";
    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final Vehicle VEHICLE = Vehicle.builder().id(VEHICLE_ID).code(VEHICLE_CODE).build();
    private static final ActiveVehicle ACTIVE_VEHICLE = ActiveVehicle.builder().userId(USER_ID).vehicleId(VEHICLE_ID).build();

    private final AuthService authService = mock(AuthService.class);
    private final FetchVehiclePort fetchVehiclePort = mock(FetchVehiclePort.class);
    private final FetchUserPort fetchUserPort = mock(FetchUserPort.class);
    private final SaveUserPort saveUserPort = mock(SaveUserPort.class);
    private final VehicleService vehicleService = new VehicleServiceImpl(authService, fetchVehiclePort, fetchUserPort, saveUserPort);

    @BeforeEach
    void beforeEach() {
        when(authService.getUserIdFromJwtToken(TOKEN)).thenReturn(USER_ID.toString());
    }

    @Nested
    class when_pair_vehicle {

        @Test
        void given_valid_token_and_vehicle_code_then_pair_successfully() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleById(USER_ID)).thenReturn(false);
            when(fetchUserPort.existsActiveVehicleUnderAnyUsers(VEHICLE_ID)).thenReturn(false);

            vehicleService.pair(TOKEN, VEHICLE_CODE);

            verify(saveUserPort).saveActiveVehicle(ACTIVE_VEHICLE);
        }

        @Test
        void given_invalid_vehicle_code_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.pair(TOKEN, VEHICLE_CODE))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle with code not found.");
        }

        @Test
        void given_user_already_paired_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleById(USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.pair(TOKEN, VEHICLE_CODE))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("User already paired with a vehicle.");
        }

        @Test
        void given_vehicle_already_paired_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleById(USER_ID)).thenReturn(false);
            when(fetchUserPort.existsActiveVehicleUnderAnyUsers(VEHICLE_ID)).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.pair(TOKEN, VEHICLE_CODE))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle already paired with another user.");
        }
    }

    @Nested
    class when_unpair_vehicle {

        @Test
        void given_valid_token_and_vehicle_code_then_unpair_successfully() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(true);

            vehicleService.unpair(TOKEN, VEHICLE_CODE);

            verify(saveUserPort).deleteActiveVehicle(ACTIVE_VEHICLE);
        }

        @Test
        void given_invalid_vehicle_code_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.unpair(TOKEN, VEHICLE_CODE))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle with code not found.");
        }

        @Test
        void given_user_not_paired_with_vehicle_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(false);

            assertThatThrownBy(() -> vehicleService.unpair(TOKEN, VEHICLE_CODE))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("User already paired with a vehicle.");
        }
    }
}

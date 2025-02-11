package com.tuul.test.vehicle.service;

import com.tuul.test.UnitTest;
import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.reservation.model.Reservation;
import com.tuul.test.reservation.port.FetchReservationPort;
import com.tuul.test.reservation.port.SaveReservationPort;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.ActiveVehicle;
import com.tuul.test.vehicle.model.Vehicle;
import com.tuul.test.vehicle.model.VehicleCommand;
import com.tuul.test.vehicle.port.FetchVehiclePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final SaveReservationPort saveReservationPort = mock(SaveReservationPort.class);
    private final FetchReservationPort fetchReservationPort = mock(FetchReservationPort.class);
    private final Clock fixedClock = Clock.fixed(LocalDateTime.of(2025, 2, 11, 15, 30, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    private final VehicleService vehicleService = new VehicleServiceImpl(authService, fetchVehiclePort, fetchUserPort, saveUserPort, saveReservationPort, fetchReservationPort, fixedClock);

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
                    .hasMessage("Vehicle not paired with user.");
        }
    }

    @Nested
    class when_send_command {

        @Test
        void given_valid_start_command_then_create_reservation() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(true);
            when(fetchReservationPort.existsActiveReservationForUserOrVehicle(USER_ID, VEHICLE_ID)).thenReturn(false);

            vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.START);

            verify(saveReservationPort).save(argThat(reservation ->
                    reservation.getUserId().equals(USER_ID) &&
                            reservation.getVehicleId().equals(VEHICLE_ID) &&
                            reservation.getStartTime().equals(LocalDateTime.now(fixedClock)) &&
                            reservation.getEndingLocation() == null
            ));
        }

        @Test
        void given_vehicle_code_not_found_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.START))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle with code not found.");

            verify(fetchUserPort, never()).existsActiveVehicleUnderUser(any(), any());
            verify(fetchReservationPort, never()).fetchActiveReservation(any(), any());
            verify(saveReservationPort, never()).save(any());
        }

        @Test
        void given_existing_active_reservation_then_throw_exception_on_start() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(true);
            when(fetchReservationPort.existsActiveReservationForUserOrVehicle(USER_ID, VEHICLE_ID)).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.START))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle or user already has active reservation.");
        }

        @Test
        void given_vehicle_not_paired_with_user_then_throw_business_violation_exception() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(false);

            assertThatThrownBy(() -> vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.START))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Vehicle not paired with user.");

            verify(fetchReservationPort, never()).fetchActiveReservation(any(), any());
            verify(saveReservationPort, never()).save(any());
        }

        @Test
        void given_valid_stop_command_then_end_reservation() {
            var activeReservation = Reservation.builder()
                    .id(UUID.randomUUID())
                    .userId(USER_ID)
                    .vehicleId(VEHICLE_ID)
                    .startTime(LocalDateTime.now(fixedClock).minusMinutes(15))
                    .build();

            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(true);
            when(fetchReservationPort.fetchActiveReservation(USER_ID, VEHICLE_ID)).thenReturn(Optional.of(activeReservation));

            vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.STOP);

            verify(saveReservationPort).save(argThat(reservation ->
                    reservation.getEndTime().equals(LocalDateTime.now(fixedClock)) &&
                            reservation.getCostOfReservation().compareTo(new BigDecimal("7.50")) == 0
            ));
        }

        @Test
        void given_no_active_reservation_then_throw_exception_on_stop() {
            when(fetchVehiclePort.findByCode(VEHICLE_CODE)).thenReturn(Optional.of(VEHICLE));
            when(fetchUserPort.existsActiveVehicleUnderUser(USER_ID, VEHICLE_ID)).thenReturn(true);
            when(fetchReservationPort.fetchActiveReservation(USER_ID, VEHICLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.sendCommand(TOKEN, VEHICLE_CODE, VehicleCommand.STOP))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Active reservation not found.");
        }
    }

}

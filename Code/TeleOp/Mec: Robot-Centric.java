package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import java.util.List;

@TeleOp(name = "teleOpDriving_mecanum_test1")
public class teleOpDriving_mecanum_test1 extends LinearOpMode {
    DcMotor frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor; //Declaring variables used for motors

    @Override
    public void runOpMode() {
        // Pre-run

        //Attaching the variables declared with the physical motors by name or id
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        //Flipping rotation of right wheels so each motor's positive direction is the same way
        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        //Increasing efficiency in getting data from the robot
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        //Updating the user screen
        telemetry.addData("Status", "Waiting to Start");
        telemetry.update();

        //Stops program until the button to start has been pressed
        waitForStart();

        if (opModeIsActive()) {

            while (opModeIsActive()) {
                // OpMode loop

                robotMovement();

            }
        }
    }

    private void robotMovement(){
        //Used to limit power in case of power level going over 100%
        double highestValue;

        double forwardBackwardValue = -gamepad1.left_stick_y; //Controls moving forward/backward
        double leftRightValue = gamepad1.left_stick_x * 1.1; //Controls strafing left/right       *the 1.1 multiplier is to counteract any imperfections during the strafing*
        double turningValue = gamepad1.right_stick_x; //Controls turning left/right

        //Makes sure power of each engine is not below 100% (Math cuts anything above 1.0 to 1.0, meaning you can lose values unless you change values)
        //This gets the highest possible outcome, and if it's over 1.0, it will lower all motor powers by the same ratio to make sure powers stay equal
        highestValue = Math.max(Math.abs(forwardBackwardValue) + Math.abs(leftRightValue) + Math.abs(turningValue), 1);


        //Calculates amount of power for each wheel to get the desired outcome
        //E.G. You pressed the left joystick forward and right, and the right joystick right, you strafe diagonally while at the same time turning right, creating a circular strafing motion.
        //E.G. You pressed the left joystick forward, and the right joystick left, you drive like a car and turn left
        frontLeftMotor.setPower((forwardBackwardValue + leftRightValue + turningValue) / highestValue);
        backLeftMotor.setPower((forwardBackwardValue - leftRightValue + turningValue) / highestValue);
        frontRightMotor.setPower((forwardBackwardValue - leftRightValue - turningValue) / highestValue);
        backRightMotor.setPower((forwardBackwardValue + leftRightValue - turningValue) / highestValue);
    }
}

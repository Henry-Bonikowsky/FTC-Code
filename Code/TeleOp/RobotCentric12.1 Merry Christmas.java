package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import java.util.List;

@TeleOp(name = "RobotCentric")
public class RobotCentric extends LinearOpMode {
    DcMotor frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor; //Declaring variables used for motors

    DcMotor armExtendTwo, slideMotor;
    DcMotorEx armExtend;

    Servo claw;

    double motorDampener = 1;


    @Override
    public void runOpMode() {

        //-------------------------------------------------#
        //                    PRE-PHASE                    |
        //-------------------------------------------------#

        //BULK READING

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        //Attaching the variables declared with the physical motors by name or id

        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        armExtend = (DcMotorEx) hardwareMap.get(DcMotor.class, "armExtend");
        armExtend.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armExtend.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        slideMotor = hardwareMap.get(DcMotor.class, "slide");


        claw = hardwareMap.get(Servo.class, "claw");

        //Flipping rotation of right wheels so each motor's positive direction is the same way
        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        //Updating the user screen
        telemetry.addData("Status", "Waiting to Start");
        telemetry.update();
        
        telemetry.addData("Status", "Starting");
        telemetry.update();

        //Stops program until the button to start has been pressed
        waitForStart();


        if (opModeIsActive()) {

            //-------------------------------------------------#
            //                    PRESET SETUP                 |
            //-------------------------------------------------#
            
            //armExtend.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            while (opModeIsActive()) {

                //-------------------------------------------------#
                //                     PRESETS                     |
                //-------------------------------------------------#

                //ARM ROTATION FOR SPECIMEN HANG
                //if(gamepad1.dpad_left && gamepad1.y){
                    //telemetry.addData("Preset", "Arm");
                //    armExtend.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                //    armExtend.setTargetPosition(-462);
                //}

                //-------------------------------------------------#
                //                  CLAWS + ARMS                   |
                //-------------------------------------------------#

                //LINEAR SLIDE MOVEMENT  OUT OF ORDER

                double slidePower = 0;
                if(gamepad1.y && !gamepad1.dpad_left){
                    slidePower = -0.5;
                }else if(gamepad1.a && !gamepad1.dpad_left){
                    slidePower = 0.5;
                }
                slideMotor.setPower(slidePower);

                //MOVEMENT SPEED DAMPENER

                if(gamepad1.left_bumper && motorDampener > 1){
                    motorDampener -= 1; //SPEED UP
                }else if(gamepad1.right_bumper && motorDampener < 2){
                    motorDampener += 1; //SLOW DOWN
                }

                //CLAW USAGE

                claw.setPosition(gamepad1.right_trigger);

                //ARM ROTATION

                //if(!armExtend.isBusy()){ //DONT WANT TO MESS UP PRESET
                double armSpeed = 0;
                if(gamepad1.dpad_up){
                    armSpeed = -400;
                }else if(gamepad1.dpad_down){
                    armSpeed = 400;
                }
                extendSlides(armSpeed);
                //}



                //-------------------------------------------------#
                //                  ROBOT MOVEMENT                 |
                //-------------------------------------------------#

                robotMovement();


                telemetry.update();
            }
        }
    }

    private void extendSlides(double speed){
        armExtend.setVelocity(speed);
        telemetry.addData("Arm Tick", armExtend.getCurrentPosition());
        //armExtendTwo.setPower(speed);
    }

    private void robotMovement(){
        //Used to limit power in case of power level going over 100%
        double highestValue;

        double forwardBackwardValue = -gamepad1.left_stick_y; //Controls moving forward/backward
        double leftRightValue = gamepad1.left_stick_x * 1.1; //Controls strafing left/right       *the 1.1 multiplier is to counteract any imperfections during the strafing*
        double turningValue = gamepad1.right_stick_x * -1; //Controls turning left/right

        //Makes sure power of each engine is not below 100% (Math cuts anything above 1.0 to 1.0, meaning you can lose values unless you change values)
        //This gets the highest possible outcome, and if it's over 1.0, it will lower all motor powers by the same ratio to make sure powers stay equal
        highestValue = Math.max(Math.abs(forwardBackwardValue) + Math.abs(leftRightValue) + Math.abs(turningValue), 1);


        //Calculates amount of power for each wheel to get the desired outcome
        //E.G. You pressed the left joystick forward and right, and the right joystick right, you strafe diagonally while at the same time turning right, creating a circular strafing motion.
        //E.G. You pressed the left joystick forward, and the right joystick left, you drive like a car and turn left
        if(highestValue > 0.2){
            frontLeftMotor.setPower((forwardBackwardValue + leftRightValue + turningValue) / highestValue / motorDampener);
            backLeftMotor.setPower((forwardBackwardValue - leftRightValue + turningValue) / highestValue / motorDampener);
            frontRightMotor.setPower((forwardBackwardValue - leftRightValue - turningValue) / highestValue / motorDampener);
            backRightMotor.setPower((forwardBackwardValue + leftRightValue - turningValue) / highestValue / motorDampener);
        }

    }
}

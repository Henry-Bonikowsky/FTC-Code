package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.hardware.lynx.LynxModule;

import java.io.File;
import java.util.ArrayList;

import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;


@TeleOp(name = "Record_Autonomous")
public class Record_Autonomous extends LinearOpMode {
    //List of each "Frame" of the recording | Each frame has multiple saved values that are needed to fully visualize it
    ArrayList<HashMap<String, Double>> recording = new ArrayList<>();


    final ElapsedTime runtime = new ElapsedTime();
    boolean isPlaying = false;
    int frameCounter = 0;
    int robotState = 0;


    //Variables for motor usage
    DcMotor frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor; //Declaring variables used for motors




    @Override
    public void runOpMode() {


        //Attaching the variables declared with the physical motors by name or id
        {
            frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
            backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
            frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
            backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
            frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        }


        //Increasing efficiency in getting data from the robot
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }


        telemetry.addData("Status", "Waiting to Start");
        telemetry.update();

        waitForStart();



        if (opModeIsActive()) {
            while (opModeIsActive()) {


                telemetry.addData("RobotState", robotState + "");
                telemetry.addData("Runtime", runtime.time());
                if (gamepad1.start && robotState == 0) {
                    robotState = 1;
                    runtime.reset();
                    telemetry.addData("Status", "Recording");
                    telemetry.addData("Time until recording end", 15 - runtime.time() + "");
                }
                 else if(robotState == 0){
                    telemetry.addData("Status", "Waiting to start recording");
                    telemetry.addData("Version", "1");
                }


                else if(robotState == 1){
                    if(15 - runtime.time() > 0){
                        telemetry.addData("Status", "Recording");
                        telemetry.addData("Time until recording end", 15 - runtime.time() + "");
                        HashMap<String, Double> values = robotMovement();
                        recording.add(values);
                    }else{
                        robotState = 2;
                    }
                }


                else if(robotState == 2){
                    telemetry.addData("Status", "Waiting to play Recording" + recording.size());
                    telemetry.addData("Time", runtime.time() + "");
                    if (gamepad1.start){
                        runtime.reset();
                        robotState = 3;
                        telemetry.addData("Status", "Playing Recording");
                        telemetry.update();
                        isPlaying = true;
                        playRecording(recording);
                    }
                }


                else if(robotState == 3){
                    if(gamepad1.x){
                        isPlaying = false;
                    }
                    if(isPlaying){
                        playRecording(recording);
                    }else{
                        robotState = 4;
                        telemetry.addData("Status", "Done Recording play-back");
                        telemetry.addData("Save to file", "Press start to save");
                        telemetry.update();
                    }
                }


                else if(robotState == 4){
                    if(gamepad1.start){
                        telemetry.addData("Status", "Saving File");
                        boolean recordingIsSaved = false;
                        String path = String.format("%s/FIRST/data/Auto_Left_Collection.fil",
                                Environment.getExternalStorageDirectory().getAbsolutePath());



                        telemetry.clearAll();
                        telemetry.addData("Status", saveRecording(recording, path));
                        telemetry.update();


                    }
                }



                telemetry.update();
            }


        }
    }

    public String saveRecording(ArrayList<HashMap<String, Double>> recording, String path){
        String rv = "Save Complete";

        try {
            File file = new File(path);

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(recording);
            oos.close();
        }
        catch(IOException e){
            rv = e.toString();
        }

        return rv;
    }




    //Think of each frame as a collection of every input the driver makes in one moment, saved like a frame in a video is
    private void playRecording(ArrayList<HashMap<String, Double>> recording){
        //Gets the correct from from the recording


        double largestTime = 0;
        int largestNum = 0;
        int correctTimeStamp = 0;
        for(int i = 0; i < recording.size();i++){
            if(recording.get(i).get("time") > largestTime){
                if(recording.get(i).get("time") <= runtime.time()){
                    largestTime = recording.get(i).get("time");
                    largestNum = i;
                }
                else{
                    correctTimeStamp = largestNum;
                }
            }
        }
        telemetry.addData("correctTimeStamp", correctTimeStamp + "");
        telemetry.update();
        HashMap<String, Double> values = recording.get(correctTimeStamp);


        double forwardBackwardValue = values.getOrDefault("rotY", 0.0);
        double leftRightValue = values.getOrDefault("rotX", 0.0);
        double turningValue = values.getOrDefault("rx", 0.0);


        double highestValue = Math.max(Math.abs(forwardBackwardValue) + Math.abs(leftRightValue) + Math.abs(turningValue), 1);




        //Calculates amount of power for each wheel to get the desired outcome
        //E.G. You pressed the left joystick forward and right, and the right joystick right, you strafe diagonally while at the same time turning right, creating a circular strafing motion.
        //E.G. You pressed the left joystick forward, and the right joystick left, you drive like a car and turn left
        if(highestValue >= 0.1){
            frontLeftMotor.setPower((forwardBackwardValue + leftRightValue + turningValue) / highestValue);
            backLeftMotor.setPower((forwardBackwardValue - leftRightValue + turningValue) / highestValue);
            frontRightMotor.setPower((forwardBackwardValue - leftRightValue - turningValue) / highestValue);
            backRightMotor.setPower((forwardBackwardValue + leftRightValue - turningValue) / highestValue);
        }
    }




    //Simple robot movement
    private HashMap<String, Double> robotMovement() {
        frameCounter++;
        HashMap<String, Double> values = new HashMap<>();






        double highestValue;


        double forwardBackwardValue = -gamepad1.left_stick_y; //Controls moving forward/backward
        double leftRightValue = gamepad1.left_stick_x * 1.1; //Controls strafing left/right       *the 1.1 multiplier is to counteract any imperfections during the strafing*
        double turningValue = gamepad1.right_stick_x; //Controls turning left/right
        forwardBackwardValue /= 2;
        leftRightValue /= 2;
        turningValue /= 2;


        values.put("rotY", forwardBackwardValue);
        values.put("rotX", leftRightValue);
        values.put("rx", turningValue);
        values.put("time", runtime.time());








        //Makes sure power of each engine is not below 100% (Math cuts anything above 1.0 to 1.0, meaning you can lose values unless you change values)
        //This gets the highest possible outcome, and if it's over 1.0, it will lower all motor powers by the same ratio to make sure powers stay equal
        highestValue = Math.max(Math.abs(forwardBackwardValue) + Math.abs(leftRightValue) + Math.abs(turningValue), 1);




        //Calculates amount of power for each wheel to get the desired outcome
        //E.G. You pressed the left joystick forward and right, and the right joystick right, you strafe diagonally while at the same time turning right, creating a circular strafing motion.
        //E.G. You pressed the left joystick forward, and the right joystick left, you drive like a car and turn left
        if(highestValue >= 0.3){
            frontLeftMotor.setPower((forwardBackwardValue + leftRightValue + turningValue) / highestValue);
            backLeftMotor.setPower((forwardBackwardValue - leftRightValue + turningValue) / highestValue);
            frontRightMotor.setPower((forwardBackwardValue - leftRightValue - turningValue) / highestValue);
            backRightMotor.setPower((forwardBackwardValue + leftRightValue - turningValue) / highestValue);
        }


        return values;
    }






}

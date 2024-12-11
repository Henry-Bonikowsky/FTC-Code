import com.qualcomm.hardware.lynx.LynxModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import android.os.Environment;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

@Autonomous
public class Play_Autonomous extends LinearOpMode {

    ArrayList<HashMap<String, Double>> recording = new ArrayList<>();
    final ElapsedTime runtime = new ElapsedTime();
    DcMotor frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor; //Declaring variables used for motors


    public void runOpMode() {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
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

        loadDatabase();
        waitForStart();
        if (opModeIsActive()) {
            runtime.reset();
            while(opModeIsActive()){
                playRecording(recording);
            }

        }

    }


//Reads the saved recording from file. You have to change the file path when saving and reading.
    private boolean loadDatabase() {
        boolean loadedProperly = false;
        String path = String.format("%s/FIRST/data/Auto_Right_Collection.fil",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            recording = (ArrayList<HashMap<String, Double>>) ois.readObject();
            if(recording instanceof ArrayList){
                telemetry.addData("Update", "It worked!");
            }else{
                telemetry.addData("Update", "Did not work smh");
            }
            ois.close();
        } catch (IOException e) {
            telemetry.addData("Error", "IOException");
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            telemetry.addData("Error", "ClassNotFoundException");
            e.printStackTrace();
        }
        telemetry.addData("recording", recording.toString());
        telemetry.update();
        return loadedProperly;
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

}

/*
 Example sketch for the Xbox ONE USB library - by guruthree, based on work by
 Kristian Lauszus.
 */
int joySerial[10];
#include <XBOXONE.h>

// Satisfy the IDE, which needs to see the include statment in the ino too.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif
#include <SPI.h>

USB Usb;
XBOXONE Xbox(&Usb);

void setup() {
  Serial.begin(115200);
  while (!Serial); // Wait for serial port to connect - used on Leonardo, Teensy and other boards with built-in USB CDC serial connection
  if (Usb.Init() == -1) {
    Serial.print(F("\r\nOSC did not start"));
    while (1); //halt
  }
  Serial.print(F("\r\nXBOX USB Library Started"));
}
void loop() {
  Usb.Task();
  if (Xbox.XboxOneConnected) {
    
//    if (Xbox.getAnalogHat(LeftHatX) > 7500 || Xbox.getAnalogHat(LeftHatX) < -7500 || Xbox.getAnalogHat(LeftHatY) > 7500 || Xbox.getAnalogHat(LeftHatY) < -7500 || Xbox.getAnalogHat(RightHatX) > 7500 || Xbox.getAnalogHat(RightHatX) < -7500 || Xbox.getAnalogHat(RightHatY) > 7500 || Xbox.getAnalogHat(RightHatY) < -7500) {
//      if (Xbox.getAnalogHat(LeftHatX) > 7500 || Xbox.getAnalogHat(LeftHatX) < -7500) {
//        Serial.print(F("LeftHatX: "));
//        Serial.print(Xbox.getAnalogHat(LeftHatX));
//        Serial.print("\t");
//      }
//      if (Xbox.getAnalogHat(LeftHatY) > 7500 || Xbox.getAnalogHat(LeftHatY) < -7500) {
//        Serial.print(F("LeftHatY: "));
//        Serial.print(Xbox.getAnalogHat(LeftHatY));
//        Serial.print("\t");
//      }
//      if (Xbox.getAnalogHat(RightHatX) > 7500 || Xbox.getAnalogHat(RightHatX) < -7500) {
//        Serial.print(F("RightHatX: "));
//        Serial.print(Xbox.getAnalogHat(RightHatX));
//        Serial.print("\t");
//      }
//      if (Xbox.getAnalogHat(RightHatY) > 7500 || Xbox.getAnalogHat(RightHatY) < -7500) {
//        Serial.print(F("RightHatY: "));
//        Serial.print(Xbox.getAnalogHat(RightHatY));
//      }
//      Serial.println();
//    }
//
//    if (Xbox.getButtonPress(L2) > 0 || Xbox.getButtonPress(R2) > 0) {
//      if (Xbox.getButtonPress(L2) > 0) {
//        Serial.print(F("L2: "));
//        Serial.print(Xbox.getButtonPress(L2));
//        Serial.print("\t");
//      }
//      if (Xbox.getButtonPress(R2) > 0) {
//        Serial.print(F("R2: "));
//        Serial.print(Xbox.getButtonPress(R2));
//        Serial.print("\t");
//      }
//      Serial.println();
//    }

    joySerial[3] = Xbox.getAnalogHat(LeftHatX)/364;
    joySerial[4] = Xbox.getAnalogHat(LeftHatY)/364;
    joySerial[5] = Xbox.getAnalogHat(RightHatX)/364;
    joySerial[6] = Xbox.getAnalogHat(RightHatY)/364;
    joySerial[7] = Xbox.getButtonPress(L2)/4;
    joySerial[8] = Xbox.getButtonPress(R2)/4;

    if (Xbox.getButtonClick(UP)){
       joySerial[1] = 0;
      //Serial.println(F("Up"));
      }
    if (Xbox.getButtonClick(DOWN)){
      joySerial[1] = 3;
      //Serial.println(F("Down"));
      }
    if (Xbox.getButtonClick(LEFT)){
      joySerial[1] = 1;
      //Serial.println(F("Left"));
      }
    if (Xbox.getButtonClick(RIGHT)){
      joySerial[1] = 2;
      //Serial.println(F("Right"));
      }

    if (Xbox.getButtonClick(START)){
      joySerial[2] = 108;
      //Serial.println(F("Start"));
    }
    if (Xbox.getButtonClick(BACK)){
      joySerial[2] = 4;
      //Serial.println(F("Back"));
    }
//    if (Xbox.getButtonClick(XBOX)){
//      //Serial.println(F("Xbox"));
//    }
//    if (Xbox.getButtonClick(SYNC)){
//      //Serial.println(F("Sync"));
//    }

    if (Xbox.getButtonClick(L1)){
      joySerial[2] = 102;
      //Serial.println(F("L1"));
    }
    if (Xbox.getButtonClick(R1)){
      joySerial[2] = 103;
      //Serial.println(F("R1"));
    }
//    if (Xbox.getButtonClick(L2)){
//      Serial.println(F("L2"));
//    }
//    if (Xbox.getButtonClick(R2)){
//      Serial.println(F("R2"));
//    }
    if (Xbox.getButtonClick(L3)){
      joySerial[2] = 106;
      //Serial.println(F("L3"));
    }
    if (Xbox.getButtonClick(R3)){
      joySerial[2] = 107;
      //Serial.println(F("R3"));
    }


    if (Xbox.getButtonClick(A)){
      joySerial[2] = 96;
      //Serial.println(F("A"));
    }
    if (Xbox.getButtonClick(B)){
      joySerial[2] = 97;
      //Serial.println(F("B"));
    }
    if (Xbox.getButtonClick(X)){
      joySerial[2] = 99;
      //Serial.println(F("X"));
    }
    if (Xbox.getButtonClick(Y)){
      joySerial[2] = 100;
      //Serial.println(F("Y"));
    }

   Serial.print(joySerial[1]);
   Serial.print(",");
   Serial.print(joySerial[2]);
   Serial.print(",");
   Serial.print(joySerial[3]);
   Serial.print(",");
   Serial.print(joySerial[4]);
   Serial.print(",");
   Serial.print(joySerial[5]);
   Serial.print(",");
   Serial.print(joySerial[6]);
   Serial.print(",");
   Serial.print(joySerial[7]);
   Serial.print(",");
   Serial.print(joySerial[8]);
   Serial.print(";");
   Serial.println();
   
  }

  delay(2);
}

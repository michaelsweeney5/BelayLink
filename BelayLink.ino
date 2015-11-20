#include <VirtualWire.h>
#include <SoftwareSerial.h>
const int led = 3;
//Message types
const int offBelay = 1;
const int haulingRope = 2;
const int thatsMe = 3;
const int onBelay = 4;
const int climbOn = 5;
//Radio TX/RX
const int radioIn = 8;
const int radioOut = 11;
//Bluetooth Serial
SoftwareSerial mySerial(4,5); // RX, TX

void setup()
{ 
  //Setup both serial interfaces
  Serial.begin(9600);
  mySerial.begin(9600);
  //this LED for beta testing/demonstration
  pinMode(led, OUTPUT);

  //Virtual Wire RF setup
  //receiver block
  vw_set_ptt_inverted(true);
  vw_set_rx_pin( 8 ); //input pin wired to pro mini
  vw_setup( 4800 ); //input BPS
  vw_rx_start(); //start receiver
  //transmitter block
  vw_set_ptt_inverted(true);
  vw_set_tx_pin( 11 ); //output pin wired to pro mini
  vw_setup( 4800 ); //output BPS
}


void loop()
{
  //Initialization - includes dipswitch values or "frequencies"
  //Can be configured to avoid conflicts between other units
  int numToSend = 0;
  int received = 0;
  char dipValue[3] = {'0','0','1'};
  char warmUp[7] = {0x00,0xA,0x5,0x00,0xA,0x5,0x00};
  numToSend = 0;
  char message[7] = {'s','t','a','r','t',' ','#'};
  char echoResponse[7] = {'0','R','0','0','1','6','#'};
  message[2] = dipValue[0];
  message[3] = dipValue[1];
  message[4] = dipValue[2];
  echoResponse[2] = dipValue[0];
  echoResponse[3] = dipValue[1];
  echoResponse[4] = dipValue[2];

  if(mySerial.available()) {
    
    received=mySerial.read();
    //Set a transmit message
    message[0] = 'T';
    message[1] = '0';
    if(received == '1')
      message[5]='1';
    else if(received == '2')
      message[5]='2';
    else if(received == '3')
      message[5]='3';
    else if(received == '4')
      message[5]='4';
    else if(received == '5')
      message[5]='5';
      
    //Warm up wire, send message
    vw_send((uint8_t *)warmUp, 7);
    vw_wait_tx();
    vw_send((uint8_t *)message, 7);
    vw_wait_tx();
    vw_send((uint8_t *)message, 7);
    vw_wait_tx();
  }
  
  uint8_t buf[VW_MAX_MESSAGE_LEN];
  uint8_t buflen = VW_MAX_MESSAGE_LEN;
  if(vw_get_message(buf, &buflen))
  {
    //Count variable for testing initial unit
    int count=0;
    if(buf[5] == '1') {
      count=offBelay;
      mySerial.println("Off Belay");
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == '2') {
      count=haulingRope;
      mySerial.println("Hauling Rope");
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == '3') {
      count=thatsMe;
      mySerial.println("That's Me");
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == '4') {
      count=onBelay;
      mySerial.println("On Belay");
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == '5') {
      count=climbOn;
      mySerial.println("Climb On");
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == '6') {
      //received a confirmation response
      digitalWrite(led,HIGH);
      delay(250);
      digitalWrite(led,LOW);
      delay(250);
      mySerial.println("Got it!");
    }

    //for initial testing
    int i;
    for(i=0;i<count;i++) {
      digitalWrite(led,HIGH);
      delay(500);
      digitalWrite(led,LOW);
      delay(500);
    }
  }

}

#include <VirtualWire.h>

const int offBelay = 3;
const int haulingRope = 4;
const int thatsMe = 5;
const int onBelay = 6;
const int climbOn = 7;
const int selectButton = 12;
const int sendButton = 13;
const int radioIn = 8;
const int radioOut = 11;

void setup()
{ //setup code, runs once
  Serial.begin(9600);
  pinMode(offBelay, OUTPUT);
  pinMode(haulingRope, OUTPUT);
  pinMode(thatsMe, OUTPUT);
  pinMode(onBelay, OUTPUT);
  pinMode(climbOn, OUTPUT);
  pinMode(selectButton, INPUT);
  pinMode(sendButton, INPUT);
  pinMode(radioIn, INPUT);
  pinMode(radioOut, OUTPUT);
  digitalWrite(offBelay, HIGH);
  digitalWrite(haulingRope, HIGH);
  digitalWrite(thatsMe, HIGH);
  digitalWrite(onBelay, HIGH);
  digitalWrite(climbOn, HIGH);
  delay(750);
  digitalWrite(offBelay, LOW);
  digitalWrite(haulingRope, LOW);
  digitalWrite(thatsMe, LOW);
  digitalWrite(onBelay, LOW);
  digitalWrite(climbOn, LOW);
  
  //receiver block
  vw_set_ptt_inverted(true);
  vw_set_rx_pin( 8 ); //input pin
  vw_setup( 4800 ); //input BPS
  vw_rx_start(); //start receiver
  //transmitter block
  vw_set_ptt_inverted(true);
  vw_set_tx_pin( 11 ); //output pin
  vw_setup( 4800 ); //output BPS
}


void loop()
{
  //Initialization - includes dipswitch values or "frequencies"
  int buttonState = 0;
  int lastButtonState = 0;
  int buttonPushCounter = 0;
  int numToDisplay = 0;
  int numToSend = 0;
  char dipValue[3] = {'0','0','1'};
  char warmUp[7] = {0x00,0xA,0x5,0x00,0xA,0x5,0x00};
  numToSend = 0;
  char message[7] = {'s','t','a','r','t',' ','#'};
  char echoResponse[7] = {'0','R','0','0','1','#','#'};
  message[2] = dipValue[0];
  message[3] = dipValue[1];
  message[4] = dipValue[2];
  echoResponse[2] = dipValue[0];
  echoResponse[3] = dipValue[1];
  echoResponse[4] = dipValue[2];
  
  //Interface code
  int count=0;
  buttonState = digitalRead(selectButton);
  if(buttonState != lastButtonState){
    if (digitalRead(selectButton) == HIGH) {
      //count variable will break while loop if needed
      while(digitalRead(sendButton) == LOW && count<30)
      {
        //Let user cycle through message choices
        buttonState = digitalRead(selectButton);
        if(buttonState != lastButtonState){
          buttonPushCounter++;
          buttonPushCounter = buttonPushCounter % 6;
          numToDisplay = buttonPushCounter+2;
        }
        if(numToDisplay > 2)
        {
          digitalWrite(numToDisplay, HIGH);
          delay(750);
          digitalWrite(numToDisplay, LOW);
        }
        count++;
      }
      numToSend = numToDisplay;
    }
  }
  
  //If loop timed out, don't send message
  if(count < 30)
  {  
    //Test if we need to transmit a message
    if(numToSend < 8 && numToSend > 2)
    {
      //Set a transmit message
      message[0] = 'T';
      message[1] = '0';
      if(numToSend == 3)
        message[5]='3';
      else if(numToSend == 4)
        message[5]='4';
      else if(numToSend == 5)
        message[5]='5';
      else if(numToSend == 6)
        message[5]='6';
      else if(numToSend == 7)
        message[5]='7';
      else
        message[5]='x';
      
      //Warm up wire, send message
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)message, 7);
      vw_wait_tx();
      vw_send((uint8_t *)message, 7);
      vw_wait_tx();
    }
  }
  
  //save the current state as the last state,
  //for next time through the loop
  lastButtonState = buttonState;
  buttonPushCounter = 0;
  
  uint8_t buf[VW_MAX_MESSAGE_LEN];
  uint8_t buflen = VW_MAX_MESSAGE_LEN;
  if(vw_get_message(buf, &buflen))
  {
    //Debug to serial
    Serial.println("Valid message received");
    for(int i=0;i<8;i++)
      Serial.println(buf[i]);
    
    //Test message for appropriate character
    if(buf[5] == 51){
      digitalWrite(offBelay, HIGH);
      delay(1000);
      digitalWrite(offBelay, LOW);
      delay(500);
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == 52){
      digitalWrite(haulingRope, HIGH);
      delay(1000);
      digitalWrite(haulingRope, LOW);
      delay(500);
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == 53){
      digitalWrite(thatsMe, HIGH);
      delay(1000);
      digitalWrite(thatsMe, LOW);
      delay(500);
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == 54){
      digitalWrite(onBelay, HIGH);
      delay(1000);
      digitalWrite(onBelay, LOW);
      delay(500);
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[5] == 55){
      digitalWrite(climbOn, HIGH);
      delay(1000);
      digitalWrite(climbOn, LOW);
      delay(500);
      vw_send((uint8_t *)warmUp, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
      vw_send((uint8_t *)echoResponse, 7);
      vw_wait_tx();
    }
    else if(buf[1] == 82){
      //Received an echo confirmation, quick flash
      digitalWrite(offBelay , HIGH);
      digitalWrite(haulingRope , HIGH);
      digitalWrite(thatsMe , HIGH);
      digitalWrite(onBelay , HIGH);
      digitalWrite(climbOn , HIGH);
      delay(100);
      digitalWrite(offBelay , LOW);
      digitalWrite(haulingRope , LOW);
      digitalWrite(thatsMe , LOW);
      digitalWrite(onBelay , LOW);
      digitalWrite(climbOn , LOW);      
    }
  }
}

#!/usr/bin/bash

gpio export 2 out # EN_LEFT
gpio export 3 out 
gpio export 4 out 
gpio export 17 out # EN_RIGHT
gpio export 27 out 
gpio export 22 out 

gpio -g write 3 1
gpio -g write 4 0
gpio -g write 27 1
gpio  -g write 22 0
gpio pwm 2 1023 
gpio pwm 17 1023
sleep 2


        
        
        
      

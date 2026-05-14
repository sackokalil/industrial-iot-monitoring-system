import { Component, OnInit } from '@angular/core';
import {NgIf, NgForOf, NgClass, NgSwitch, NgSwitchCase, NgSwitchDefault} from '@angular/common';
import { MqttService } from './services/mqtt.service';
import { LampMessageData  } from './services/mqtt.service';
import { TemperatureMessageData  } from './services/mqtt.service';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NgIf, NgForOf, NgClass, NgSwitch, NgSwitchCase, NgSwitchDefault], // 🧠 et ici, tu les déclares tous les trois
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  // attributes
  lampes!: boolean[] ;
  ist!: number;
  soll!: number;
  diff!: number;
  lightStatus!: string ;
  mod!:string;
  pollingInterval: any;

  //Mode commande attributes
  modeDisable0! : boolean;
  modeDisable1!:boolean;
  modeDisable2! :boolean;
  modeDisable3! :boolean;


  //connection control attributes
  connected = false;
  disconnected = true;
  reset  = false;




  constructor(private mqttService: MqttService) {
  }

  ngOnInit(): void {
    //initializatiion of the attributes
    this.initialization();
    this.initModeControl();
  }

  initialization(){
    this.lampes = new Array(16).fill(false);
    this.ist= 0;
    this.soll = 0;
    this.diff = 0;
    this.lightStatus = "";
    this.mod = "default";
  }
  initModeControl(){
  this.modeDisable0 = true;
  this.modeDisable1 = true;
  this.modeDisable2 = true;
  this.modeDisable3 = true;
}

  connect() {
    this.mqttService.connect().subscribe(() => {
      console.log(" MQTT Client connected");
    });

    this.connected = true;
    this.disconnected = false;
    this.reset = false
    this.modeDisable0 = false;
    this.modeDisable1 = false;
    this.modeDisable2 = false;
    this.modeDisable3 = false;

    this.startPolling();

  }


  sendMode(mode: string) {

    this.mqttService.sendMode(mode).subscribe(() => {
      console.log("✅ Mode sent :", mode);
    });
    switch (mode){
      case  '0':
        this.mod = '0';
        this.modeDisable0=true;
        this.modeDisable1 = false;
        this.modeDisable2 = false;
        this.modeDisable3 = false;
        break;
      case  '1':
        this.mod = '1';
        this.modeDisable0=false;
        this.modeDisable1 = true;
        this.modeDisable2 = false;
        this.modeDisable3 = false;
        break;
      case  '2':
        this.mod = '2';
        this.modeDisable0=false;
        this.modeDisable1 = false;
        this.modeDisable2 = true;
        this.modeDisable3 = false;
        break;
      case  '3':
        this.mod = '3';
        this.modeDisable0=false;
        this.modeDisable1 = false;
        this.modeDisable2 = false;
        this.modeDisable3 = true;
        break;
      default: this.mod ="default";
    }
  }

  disconnect() {

    this.mqttService.disconnect().subscribe(() => {
      console.log(" MQTT Client disconnected");
    });
    this.disconnected = true;
    this.reset = false;
    this.connected = false;
    this.mod = "lastMode"
    this.initModeControl();
    clearInterval(this.pollingInterval);
  }

  resetAll(){
    this.reset = true;
    this.connected = false;
    this.disconnected = true;
    this.initModeControl();
    this.initialization();
    this.mod = "lastMode"

    clearInterval(this.pollingInterval);
  }

  /*disconnect() {
    this.connected = false;
    this.mqttService.disconnect().subscribe(()=>{
      console.log("🛑 Disconnected from MQTT broker");

    });
    clearInterval(this.pollingInterval);


  }*/

  startPolling(): void {
    this.updateData();
    this.pollingInterval = setInterval(() => this.updateData(), 1000);
  }

  updateData(): void {
    this.mqttService.getStatus().subscribe(data => {
      this.lampes = data.status.split('').reverse().map((b: string) => b === '1');
      /*this.lampes = data.status.split('').map((b: string) => b === '1');*/
      this.lightStatus = data.status;
    });

    this.mqttService.getIst().subscribe(data => this.ist = parseFloat(data.value));
    this.mqttService.getSoll().subscribe(data => this.soll = parseFloat(data.value));
    this.mqttService.getDiff().subscribe(data => this.diff = parseFloat(data.value));
  }
}

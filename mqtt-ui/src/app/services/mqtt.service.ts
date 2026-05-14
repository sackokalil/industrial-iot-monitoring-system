
/* ---------- mqtt.service.ts ---------- */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface LampMessageData  {
  status: string;
  topic: string;
  receivedAt: Date;

}

export interface TemperatureMessageData  {
  value: string;
  topic: string;
  receivedAt: Date;

}

@Injectable({
  providedIn: 'root'
})
export class MqttService {
  private baseUrl = 'http://localhost:8080/mqttApi';

  constructor(private http: HttpClient) {}

  connect(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/connect`, {});
  }

  disconnect(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/disconnect`, {});
  }
  sendMode(mode: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/sendMode/${mode}`, {});
  }

  getStatus(): Observable<LampMessageData > {
    return this.http.get<LampMessageData >(`${this.baseUrl}/status`);
  }
  getIst(): Observable<TemperatureMessageData>{
    return this.http.get<TemperatureMessageData>(`${this.baseUrl}/ist`)
  }
  getSoll():Observable<TemperatureMessageData>{
    return this.http.get<TemperatureMessageData>(`${this.baseUrl}/soll`)
  }
  getDiff():Observable<TemperatureMessageData>{
    return this.http.get<TemperatureMessageData>(`${this.baseUrl}/diff`)
  }
}


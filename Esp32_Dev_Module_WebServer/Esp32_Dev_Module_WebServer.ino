#include <WiFi.h>
#include <WebServer.h>

const char* ssid = "WIFI_NAME_REPLACE";         // ชื่อ Wi-Fi
const char* password = "WIFI_PASSWORD_REPLACE"; // รหัสผ่าน Wi-Fi

WebServer server(80); // สร้าง Web Server พอร์ต 80
const int ledPin = 2; // ขาไฟ LED

void handleOn() {
  digitalWrite(ledPin, HIGH);
  server.send(200, "text/plain", "LED IS ON");
}

void handleOff() {
  digitalWrite(ledPin, LOW);
  server.send(200, "text/plain", "LED IS OFF");
}

void setup() {
  Serial.begin(115200);
  pinMode(ledPin, OUTPUT);

  // เชื่อมต่อ Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("\nWiFi Connected!");
  Serial.print("ESP32 IP Address: ");
  Serial.println(WiFi.localIP()); // <-- ดู IP Address ตรงนี้ใน Serial Monitor!

  // ตั้งค่า Route สำหรับรับคำสั่ง
  server.on("/on", handleOn);
  server.on("/off", handleOff);

  server.begin();
}

void loop() {
  server.handleClient(); // รอรับคำสั่งจากแอป
}
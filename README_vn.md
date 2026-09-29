# RayTraceAntiXray

Paper Anti-Xray engine-mode 1 không ẩn được quặng lộ trong hang động. Plugin này ray-trace từ mắt từng người chơi và chỉ ẩn những block thực sự bị phơi.

**Paper 26.3 · 26.2 · 26.1.2 · Folia** — một JAR duy nhất.

[🇺🇸 English](README.md) · [🇻🇳 Tiếng Việt](README_vn.md)

## Yêu cầu

- Paper (hoặc fork tương thích Folia)
- Paper Anti-Xray bật `engine-mode: 1` — [docs](https://docs.papermc.io/paper/anti-xray/)
- [PacketEvents](https://modrinth.com/plugin/packetevents) (`depend` — thiếu là plugin không load)

## Cài đặt

1. Tải JAR từ [Modrinth](https://modrinth.com/plugin/forkedraytraceantixray)
2. Bỏ vào `plugins/`
3. Bật `engine-mode: 1` trong `config/paper-world-defaults.yml`
4. Restart server

> Không dùng `/reload` hay hot-swap JAR. Thay JAR hoặc đổi Paper Anti-Xray phải restart đầy đủ.

## Cấu hình

`plugins/RayTraceAntiXray/config.yml` — theo từng world, hoặc dùng preset trong [Recommended setup](https://alexteens24.github.io/RayTraceAntiXray/docs/recommended-configuration). Tham khảo đầy đủ: [Configuration](https://alexteens24.github.io/RayTraceAntiXray/docs/configuration).

## Lệnh

Cần `raytraceantixray.command.raytraceantixray`; mỗi lệnh con có permission riêng — xem [Permissions](https://alexteens24.github.io/RayTraceAntiXray/docs/permissions).

| Lệnh | Mô tả |
|------|-------|
| `/raytraceantixray reload` | Nạp lại `config.yml` |
| `/raytraceantixray timings <on\|off>` | Bật/tắt timing report |
| `/raytraceantixray reminder [dismiss\|enable]` | Hiện trạng cấu hình Anti-Xray cho operator |

## Build

```bash
./gradlew build     # -> build/libs/RayTraceAntiXray-<version>.jar
./gradlew test      # unit tests
./gradlew run26_3   # test server (cũng có run1_21_11 / run26_1_2 / run26_2)
```

Chi tiết kiến trúc multi-NMS: [FORK.md](FORK.md).

## Tài liệu

[alexteens24.github.io/RayTraceAntiXray](https://alexteens24.github.io/RayTraceAntiXray/)

## Credits

Bản gốc: [stonar96/RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray) · Ý tưởng dirty-tracking: [TauCu/RayTraceAntiXray](https://github.com/TauCu/RayTraceAntiXray)

MIT — xem [LICENSE](LICENSE).

package com.baz.gyeote.med;

public class Greeting {
    public long id;
    public String text;
    public long sentAt;         // 보낸 시각
    public long receivedAt;     // 어르신 화면에서 처음 열린 시각 (0이면 아직 수신 전)
    public long confirmedAt;    // 어르신이 '잘 읽었어요'를 누른 시각 (0이면 아직 확인 전)

    public String statusText() {
        return "보냄 " + MedRepository.clockText(sentAt)
                + " · " + (receivedAt == 0 ? "수신 전" : "수신 " + MedRepository.clockText(receivedAt))
                + " · " + (confirmedAt == 0 ? "확인 전" : "확인 " + MedRepository.clockText(confirmedAt));
    }
}
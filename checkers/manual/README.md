# Checkers – Gra sieciowa z logiką planszy i serwerem TCP

Projekt implementuje uproszczoną wersję gry planszowej chineese checkers z obsługą dwóch graczy, logiką ruchów, zbijaniem kamieni oraz komunikacją sieciową opartą o gniazda TCP. Pozwala na zbijanie otoczonych przez przeciwnika kamieni.
Aplikacja składa się z trzech głównych części:


### 1. Budowanie aplikacji

W katalogu głównym projektu (/checkers):

```bash
mvn clean package 
```
Po zbudowaniu powstaje plik: target/checkers-1.0-SNAPSHOT.jar

### 2. Uruchamianie serwera
```bash
java -cp target/checkers-1.0-SNAPSHOT.jar com.example.server.Server
```

### 3. Połączenie klienta
```bash
java -cp target/checkers-1.0-SNAPSHOT.jar com.example.ui.MainGui
```
### 4. Wykonywanie ruchów
Podczas swojej tury kliknij na punkt kratowy. Tam zostanie umieszczony Twój kamień.

### 5. Poddanie się
Aby się poddać, kliknij przycisk "RESIGN" w lewym dolnym rogu. Wtedy wygrywa Twój przeciwnik.

### 6. Pasowanie ruchu
Aby spasować, kliknij przycisk "PASS" w lewym dolnym rogu.

## 7. Zaznaczanie kamieni po podwójnym passie
Gdy gracze spasują jeden po drugim, gra przechodzi w tryb scoringu. Wtedy można oznaczyć kamienie jako martwe, klikając na nie lewym przyciskiem myszy. Martwe kamienie podświetlają się na czerwono, a żywe na zielono. Aby odznaczyć martwe kamienie, kliknij na nie prawym przyciskiem myszy.
Zaakceptuj stan żywych i martwych kamieni, klikając "ACCEPT" w lewym dolnym rogu. Jeśli dwóch graczy zaakceptuje stan, gra jest zakończona.
Gdy nie możesz dojść do porozumienia z przeciwnikiem, kliknij "RESUME" w lewym dolnym rogu. Wtedy gra jest wznawiana, a następny ruch wykonuje Twój przeciwnik.

### 8. Testy
Projekt korzysta z JUnit 5
Uruchamianie testów:
```bash
mvn test 
```


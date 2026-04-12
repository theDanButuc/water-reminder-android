# Ghid: Creare fețe de ceas pentru Huawei Watch

## 1. Pregătire cont developer

1. Mergi la [developer.huawei.com](https://developer.huawei.com)
2. Creează un cont Huawei Developer (gratuit)
3. Verifică identitatea (poate necesita document de identitate pentru publicare)

---

## 2. Instalare Watch Face Studio

1. Mergi la [Watch Face Studio](https://developer.huawei.com/consumer/en/wearengine/watchface/)
2. Descarcă versiunea pentru macOS sau Windows
3. Instalează și autentifică-te cu contul Huawei Developer

---

## 3. Compatibilitate

Verifică că Watch Face Studio suportă modelul tău:
- Huawei Watch GT 5 ✓
- Huawei Watch GT 4 ✓
- Huawei Watch 4 ✓
- Lista completă în documentația oficială

---

## 4. Design assets (înainte să deschizi Watch Face Studio)

Pregătește în **Figma / Photoshop / Illustrator**:

| Asset | Dimensiune recomandată | Format |
|-------|----------------------|--------|
| Background | 466×466 px (GT 5) | PNG |
| Ace de ceas | separate pe layere | PNG cu transparență |
| Complicații (date, pași, bătăi) | conform slot-urilor din template | PNG |
| Font custom (opțional) | — | TTF/OTF |

**Reguli design:**
- Rezoluție ecran GT 5: **466×466 px**, rotund, AMOLED
- Fundaluri negre consumă mai puțin din baterie
- Evită alb pur pe zone mari (baterie)
- Toate elementele trebuie să fie lizibile la 1.43"

---

## 5. Creare față de ceas în Watch Face Studio

1. **New Project** → alege modelul de ceas (GT 5)
2. Alege forma ecranului: **Round**
3. Importă background-ul: `Assets → Add Image`
4. Adaugă **Time components:**
   - Hours, Minutes, Seconds (ace sau digitale)
   - Configurează rotația acelor (pivot point, unghi de start)
5. Adaugă **Complications** (opțional):
   - Date, Steps, Heart Rate, Battery, Weather
   - Drag & drop din panoul din stânga
6. Adaugă **Always-On Display (AOD)** variant:
   - Versiune simplificată, doar timp, fundal negru
7. Configurează **animations** (opțional):
   - Tap interactions
   - Smooth second hand

---

## 6. Preview și testare

### Simulator (fără ceas fizic)
- Buton **Preview** în Watch Face Studio → simulator în aplicație
- Testează ziua, noaptea, AOD

### Pe ceas fizic
1. Activează **Developer Mode** pe ceas:
   - Setări → Despre → apasă de 7 ori pe numărul build
2. Conectează ceasul la PC via **Huawei Health app** (Bluetooth)
3. În Watch Face Studio: **Run → Install on Device**

---

## 7. Export și publicare

### Export fișier local
- **File → Export → .hwf** (Huawei Watch Face format)
- Poți trimite fișierul direct prietenilor (instalare manuală prin Huawei Health)

### Publicare pe AppGallery (opțional)
1. Mergi la [AppGallery Connect](https://developer.huawei.com/consumer/en/service/josp/agc/index.html)
2. **My Apps → New App → Watch Face**
3. Completează:
   - Nume, descriere, screenshots
   - Categoria: Watch Face
   - Prețul (gratuit sau plătit)
4. Upload fișierul `.hwf`
5. Submit pentru review (durează 1-3 zile lucrătoare)

---

## 8. Resurse utile

- [Watch Face Studio Download](https://developer.huawei.com/consumer/en/wearengine/watchface/)
- [Documentație oficială](https://developer.huawei.com/consumer/en/doc/harmonyos-guides/wearengine-watchface-studio-overview)
- [Huawei Design Guidelines](https://developer.huawei.com/consumer/en/doc/design/hid-V1/watch-overview-0000001050748773)
- [AppGallery Connect](https://developer.huawei.com/consumer/en/service/josp/agc/index.html)

---

*Ultima actualizare: Aprilie 2026*

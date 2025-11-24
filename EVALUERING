# EVALUERING AF ALGORITMER OG DATASTRUKTURER  
*RestaurantFinder*

## Formål
Evalueringen beskriver, hvorfor RestaurantFinder anvender MergeSort, QuickSort, KNN og to forskellige afstandsmetoder, og hvordan disse valg forbedrer relevansen og kvaliteten af søgeresultaterne i applikationen.

---

## Datastrukturer
Projektet bruger en simpel og effektiv datastruktur:

- **RestaurantDto** (immutabel) med rating, prisniveau, koordinater og postnummer.
- Backend arbejder med `List<RestaurantDto>`.
- Frontend arbejder med arrays.

Disse strukturer er lette at filtrere, sortere og præsentere, og de passer godt til projektets datamængder og funktionalitet.

---

## MergeSort (backend)
Bruges i `AlgorithmPipeline.MergeSort` til sortering efter:
- rating  
- prisniveau  
- afstand  
- weighted score  

**Hvorfor MergeSort?**
- Stabil og forudsigelig sortering.
- Garanteret **O(n log n)** uanset data.
- Fungerer perfekt med `Comparator<RestaurantDto>`.

**Evalueringsperspektiv:**  
Datasættet er lille, så algoritmens styrke ligger i stabilitet og kontrol, ikke nødvendigvis hastighed. Den giver et solidt fundament for backend-sortering uden risiko for uforudsigelige resultater.

---

## QuickSort (frontend)
Frontend bruger egen QuickSort i `script.js` til sortering efter rating, distance, prisniveau og weighted score.

**Hvorfor QuickSort?**
- Meget hurtig på små arrays.
- Gennemsnitlig **O(n log n)**.
- Gør det muligt at sortere direkte i browseren uden ekstra backend-calls.

**Evalueringsperspektiv:**  
Frontendens fokus er brugerrespons og øjeblikkelig feedback. QuickSort giver en let, funktionel og præcis implementering med minimalt overhead.

---

## Distanceberegning
To strategier anvendes for at opnå optimal balance mellem præcision og performance.

### Backend → `fastDistance()`
- Undgår `sqrt()`, og beregner kun \( \Delta x^2 + \Delta y^2 \).
- Hurtig nok til store mængder sammenligninger.
- Bruges, hvor kun rangeringen er vigtig.

### Frontend → Haversine
- Giver korrekt afstand i kilometer.
- Bruges til visning i UI, modal og kort.

**Evalueringsperspektiv:**  
Backend prioriterer hastighed i beregninger. Frontend prioriterer præcision, da afstanden skal give mening for brugeren. Kombinationen giver den bedste samlede oplevelse.

---

## K-Nearest Neighbors (KNN)
`AlgorithmPipeline.kNearest()` bruger en **PriorityQueue** til at finde de *k* nærmeste restauranter.

**Hvorfor KNN?**
- Finder de mest relevante restauranter baseret på brugerens placering.
- Effektiv kompleksitet: **O(n log k)**.
- Integrerer direkte med fastDistance for ekstra hastighed.

**Evalueringsperspektiv:**  
KNN begrænser resultatmængden til de restauranter, der faktisk er relevante, hvilket giver brugeren mere målrettede forslag end almindelig sortering.

---

## Weighted Score
Weighted score kombinerer rating, afstand og prisniveau til en samlet relevansværdi.

**Hvorfor?**
- Undgår at topresultater enten er “for langt væk” eller “lav kvalitet”.
- Skaber en mere menneskelig og realistisk prioritering.

**Evalueringsperspektiv:**  
Weighted score reducerer “støj” i søgeresultater og giver mere intuitive anbefalinger — uden at introducere komplekse modeller.

---

## Samlet vurdering
De valgte algoritmer og datastrukturer:

- Giver **stabil sortering** (MergeSort)  
- Sikrer **hurtig frontend-respons** (QuickSort)  
- Leverer **reelle og relevante** anbefalinger (KNN, weighted score)  
- Balancerer **hurtighed og præcision** (fastDistance + Haversine)  
- Matcher datasætstørrelsen uden unødig kompleksitet  

Valgene er derfor fagligt begrundede, praktiske og passende til systemets behov.

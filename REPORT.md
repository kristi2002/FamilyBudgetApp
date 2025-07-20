# Family Budget App – Relazione di Progetto

## Introduzione
La Family Budget App è un'applicazione basata su Java progettata per aiutare le famiglie a gestire efficacemente le proprie finanze. Nel mondo frenetico di oggi, tenere traccia delle spese, pianificare progetti futuri e garantire la stabilità finanziaria sono essenziali per ogni famiglia. Questa applicazione affronta queste esigenze fornendo una piattaforma completa per registrare transazioni, gestire budget, tracciare scadenze imminenti e analizzare statistiche finanziarie.
Un obiettivo chiave del progetto è fornire una soluzione che non sia solo potente e user-friendly, ma anche altamente estensibile. L'applicazione è architettata per supportare miglioramenti futuri, come l'integrazione con piattaforme mobili e web, analisi avanzate e sincronizzazione basata su cloud. Utilizzando pratiche e strumenti di sviluppo moderni—inclusi JavaFX per l'interfaccia grafica, JPA/Hibernate per la persistenza dei dati e un'architettura modulare orientata ai servizi—la Family Budget App garantisce manutenibilità, scalabilità e facilità di integrazione con nuove funzionalità.
L'app è destinata all'uso su più dispositivi ed è progettata per facilitare la sincronizzazione dei dati finanziari tra desktop e, potenzialmente, altre piattaforme. Questa flessibilità la rende una base robusta sia per le esigenze attuali che per la crescita futura, permettendo alle famiglie di prendere il controllo del proprio benessere finanziario.
---

## Funzionalità Implementate
La Family Budget App fornisce un set completo di funzionalità per supportare una gestione finanziaria familiare efficace. Le seguenti funzionalità sono state implementate in questa versione:

**Gestione delle Transazioni**
Gli utenti possono aggiungere, visualizzare e gestire transazioni finanziarie.
Ogni transazione può essere associata a uno o più tag (categorie), che possono essere organizzati gerarchicamente (es. "Utilità > Elettricità").
Supporto per transazioni programmate e ricorrenti, permettendo agli utenti di pianificare spese o entrate regolari (come abbonamenti, stipendi o rimborsi di prestiti).
I piani di ammortamento dei prestiti possono essere inseriti come una sequenza di rate programmate, con chiara separazione tra quota capitale e quota interessi.

**Gestione del Budget**
Gli utenti possono creare e gestire budget per periodi e categorie specifiche.
L'applicazione traccia spese e entrate rispetto a ogni budget, fornendo feedback in tempo reale sullo stato del budget (speso, rimanente, oltre budget).
I budget possono essere associati a tag/categorie per un tracciamento dettagliato.

**Calendario delle Scadenze**
Le spese future e le loro date di scadenza possono essere inserite e tracciate.
L'applicazione fornisce una vista dedicata per le scadenze imminenti, aiutando gli utenti a evitare pagamenti mancati e pianificare in anticipo.

**Statistiche e Confronti**
L'app elabora i dati delle transazioni e del budget per generare statistiche informative.
Gli utenti possono monitorare le loro performance finanziarie nel tempo, confrontare diversi periodi e analizzare le spese tra categorie.
Vengono fornite visualizzazioni come tabelle e grafici per una migliore comprensione.

**Ricerca e Filtri Avanzati**
Le transazioni possono essere filtrate per intervallo di date, tag (con logica AND/OR) e ricerca testuale (descrizione o importo).
Gli utenti possono localizzare rapidamente transazioni specifiche o analizzare pattern di spesa utilizzando filtri flessibili.

**Sincronizzazione (Basata su File)**
L'applicazione supporta la sincronizzazione dei dati tra dispositivi utilizzando un meccanismo di sincronizzazione basato su file.
Questo garantisce che gli utenti possano accedere e aggiornare i loro dati finanziari da più desktop, con estensibilità futura per sincronizzazione cloud o mobile.

**Caratteristiche dell'Interfaccia Utente**
L'applicazione presenta un'interfaccia moderna e intuitiva costruita con JavaFX.
Le transazioni programmate/ricorrenti sono visivamente marcate nella tabella principale delle transazioni.
Gli strumenti di filtro e ricerca avanzati sono facilmente accessibili.
L'interfaccia è progettata per chiarezza, facilità d'uso ed estensibilità futura.

## Responsabilità Identificate
La Family Budget App è progettata con un'architettura modulare che separa le responsabilità e assegna responsabilità chiare a ogni componente principale. Questo approccio garantisce manutenibilità, scalabilità e facilità di estensione futura. Le principali responsabilità sono divise come segue:

**Controller**
Gestiscono l'interfaccia utente e gestiscono le interazioni dell'utente.
Coordinano tra i componenti UI (viste JavaFX) e la logica di business sottostante.
Esempi: TransactionsController, BudgetsController, ScheduledController, SettingsController.

**Servizi**
Incapsulano la logica di business e le operazioni core dell'applicazione.
Forniscono metodi per creare, aggiornare, eliminare e interrogare oggetti di dominio come transazioni, budget e tag.
Gestiscono operazioni complesse come calcolo delle statistiche, valutazione dello stato del budget e sincronizzazione.
Esempi: TransactionService, BudgetService, ScheduledTransactionService, SyncService.

**Modelli**
Rappresentano le entità dati core dell'applicazione.
Definiscono la struttura e le relazioni di oggetti dati come transazioni, budget, tag e transazioni programmate.
Servono come ponte tra la logica dell'applicazione e il layer di persistenza dei dati.
Esempi: Transaction, Budget, Tag, ScheduledTransaction, UserSettings.

**Repository**
Gestiscono l'accesso ai dati e la persistenza.
Interagiscono con il database utilizzando JPA/Hibernate per memorizzare e recuperare entità.
Astragono i dettagli della memorizzazione dei dati dal resto dell'applicazione, permettendo facili cambiamenti al meccanismo di persistenza se necessario.
Esempi: TransactionRepository, BudgetRepository, TagRepository.

Questa chiara separazione delle responsabilità garantisce che ogni parte dell'applicazione sia focalizzata su un aspetto specifico, rendendo il codice più facile da comprendere, mantenere ed estendere.

## Classi e Interfacce Sviluppate
La Family Budget App è organizzata in classi e interfacce ben definite, ognuna con una responsabilità chiara. Di seguito è riportato un riepilogo dei componenti principali:
| Classe/Interfaccia | Responsabilità |
|--------------------------------------|---------------------------------------------------------------------|
| Transaction | Rappresenta una transazione finanziaria, inclusi data, importo, tag, ecc. |
| ScheduledTransaction | Rappresenta una transazione ricorrente o con data futura (es. rata di prestito, abbonamento). |
| Budget | Rappresenta un budget per un periodo e categoria specifica. |
| Tag | Rappresenta una categoria o etichetta per transazioni e budget; supporta la gerarchia. |
| UserSettings | Memorizza le preferenze dell'utente come valuta, locale e impostazioni di sincronizzazione. |
| TransactionService | Logica di business per gestire le transazioni (creare, aggiornare, eliminare, interrogare, calcolare saldi). |
| ScheduledTransactionService | Logica di business per gestire transazioni programmate/ricorrenti. |
| BudgetService | Logica di business per gestire i budget, inclusi stato e alert. |
| TagService | Logica di business per gestire tag/categorie. |
| SyncService (interfaccia) | Definisce metodi per sincronizzare dati tra dispositivi. |
| FileSyncService (implementa SyncService) | Fornisce implementazione di sincronizzazione basata su file. |
| StatisticsService | Fornisce metodi per generare statistiche e report. |
| ServiceFactory | Factory centralizzata per creare e gestire istanze di servizi. |
| TransactionsController | Gestisce la logica UI per la gestione delle transazioni. |
| BudgetsController | Gestisce la logica UI per la gestione dei budget. |
| ScheduledController | Gestisce la logica UI per transazioni programmate/ricorrenti. |
| SettingsController | Gestisce la logica UI per impostazioni dell'applicazione e sincronizzazione. |
| BaseController | Classe base astratta per logica condivisa dei controller. |
| TransactionRepository | Accesso ai dati per le transazioni (JPA/Hibernate). |
| BudgetRepository | Accesso ai dati per i budget (JPA/Hibernate). |
| TagRepository | Accesso ai dati per i tag (JPA/Hibernate). |
| ... | ... |
> Nota:
> Questa tabella elenca le classi e interfacce più importanti. Sono presenti anche classi di utilità aggiuntive (es. per formattazione date/valute, modelli di statistiche, ecc.) per supportare le funzionalità principali.
Ogni classe e interfaccia è progettata con una singola, chiara responsabilità, seguendo le best practice per manutenibilità ed estensibilità.

## Organizzazione dei Dati e Persistenza
La Family Budget App organizza e persiste i dati utilizzando un approccio robusto e scalabile basato su Java Persistence API (JPA) e Hibernate. Gli aspetti principali dell'organizzazione e persistenza dei dati sono i seguenti:

**Struttura dei Dati e Relazioni**
Entità: Le entità dati core includono Transaction, ScheduledTransaction, Budget, Tag e UserSettings.

Relazioni:
Le transazioni possono essere associate a uno o più Tag (categorie), permettendo categorizzazione flessibile e organizzazione gerarchica.
I budget sono collegati a tag/categorie specifiche e coprono un periodo di tempo definito.
Le ScheduledTransaction rappresentano transazioni ricorrenti o con data futura e possono anche essere taggate per categorizzazione.
I tag possono essere organizzati gerarchicamente, supportando relazioni padre-figlio per categorie annidate.
UserSettings memorizza preferenze come valuta, locale e opzioni di sincronizzazione.

**Meccanismo di Persistenza**
JPA/Hibernate: Tutte le entità principali sono annotate per JPA, abilitando object-relational mapping e integrazione seamless con database relazionali.
Repository: Ogni entità ha un repository corrispondente (es. TransactionRepository, BudgetRepository) che gestisce l'accesso ai dati e operazioni CRUD.
Relazioni tra Entità: Relazioni come one-to-many (es. un tag con multiple transazioni) e many-to-many (es. transazioni con multiple tag) sono mappate utilizzando annotazioni JPA.

**Sincronizzazione e Backup**
Sincronizzazione Basata su File: L'applicazione supporta la sincronizzazione dei dati tra dispositivi utilizzando un meccanismo di sincronizzazione basato su file. Questo permette agli utenti di mantenere i loro dati finanziari consistenti su più desktop.
Estensibilità: Il meccanismo di sincronizzazione è progettato per essere estensibile, permettendo futura integrazione con soluzioni di sincronizzazione basate su cloud o mobile.
Backup: I dati possono essere salvati copiando il database sottostante o i file di sincronizzazione, garantendo sicurezza dei dati e recupero.

**Integrità e Consistenza dei Dati**
Transazioni: Tutti i cambiamenti ai dati (aggiungere, aggiornare, eliminare) sono gestiti attraverso classi di servizio, garantendo che le regole di business e l'integrità dei dati siano applicate.
Operazioni Atomiche: Le transazioni del database sono utilizzate per garantire che le operazioni siano atomiche e consistenti, riducendo il rischio di corruzione dei dati.
Questo approccio garantisce che i dati siano ben strutturati, persistenti e pronti per la crescita futura, fornendo anche meccanismi per backup e sincronizzazione multi-dispositivo.

## Meccanismi di Estensibilità e Integrazione
La Family Budget App è progettata pensando all'estensibilità e all'integrazione futura. L'architettura e il codice utilizzano diverse strategie per garantire che nuove funzionalità, tipi di dispositivi e integrazioni possano essere aggiunti con sforzo minimo:

**Uso di Interfacce e Classi Astratte**
La logica di business core è definita attraverso interfacce (es. SyncService, BudgetService, TransactionService), permettendo multiple implementazioni.
Classi base astratte (come BaseController) forniscono funzionalità condivise per i controller UI, abilitando riuso del codice e comportamento consistente.

**Pattern Factory e Service**
La classe ServiceFactory centralizza la creazione e gestione di istanze di servizi. Questo rende facile scambiare o estendere implementazioni di servizi (es. sostituire FileSyncService con un servizio di sincronizzazione basato su cloud).
I servizi sono iniettati nei controller, disaccoppiando la logica UI dalla logica di business e rendendo più facile testare ed estendere.

**Architettura Modulare e a Livelli**
L'applicazione è organizzata in livelli chiari: UI (controller), logica di business (servizi), dati (modelli/entità) e persistenza (repository).
Ogni livello interagisce con gli altri attraverso interfacce ben definite, rendendo semplice aggiungere nuove funzionalità o sostituire componenti esistenti.

**Aggiunta di Nuove Funzionalità o Integrazioni**
Esempio: Aggiungere Sincronizzazione Cloud
Implementare una nuova classe (es. CloudSyncService) che implementa l'interfaccia SyncService.
Registrare il nuovo servizio nel ServiceFactory.
Il resto dell'applicazione può utilizzare il nuovo metodo di sincronizzazione senza alcun cambiamento alla UI o logica di business.
Esempio: Aggiungere Supporto Mobile o Web
Il design modulare permette lo sviluppo di nuovi front-end (es. app mobile, app web) che possono interagire con gli stessi layer di servizi e dati.

**Design Compatibile con Plugin**
L'uso di interfacce e factory rende possibile aggiungere plugin o estensioni (es. nuovi moduli di analisi, strumenti di import/export) senza modificare il codice core.

**Configurazione e Impostazioni**
Le preferenze dell'utente e le impostazioni dell'applicazione sono gestite attraverso l'entità UserSettings, rendendo facile aggiungere nuove opzioni configurabili in futuro.
Questo design estensibile garantisce che la Family Budget App possa evolversi per soddisfare nuovi requisiti, integrarsi con altri sistemi e supportare piattaforme aggiuntive con refactoring minimo.

## Principi SOLID e Qualità del Codice
La Family Budget App è sviluppata con una forte enfasi sulla qualità del codice e l'aderenza ai principi SOLID, garantendo manutenibilità, scalabilità e facilità di sviluppo futuro.

**Principio di Responsabilità Singola (SRP)**
Ogni classe e metodo nell'applicazione è progettato per avere una singola, ben definita responsabilità.
Per esempio, i controller gestiscono la logica UI, i servizi gestiscono le operazioni di business e i modelli rappresentano entità dati.

**Principio Aperto/Chiuso (OCP)**
Il sistema è aperto per l'estensione ma chiuso per la modifica.
Nuove funzionalità (come metodi di sincronizzazione aggiuntivi o moduli di analisi) possono essere aggiunte implementando interfacce senza alterare il codice esistente.

**Principio di Sostituzione di Liskov (LSP)**
Interfacce e classi astratte sono utilizzate così che nuove implementazioni possano essere sostituite senza influenzare la correttezza dell'applicazione.
Per esempio, qualsiasi classe che implementa SyncService può essere utilizzata intercambiabilmente nell'applicazione.

**Principio di Segregazione delle Interfacce (ISP)**
Le interfacce sono progettate per essere specifiche alle esigenze dei client, evitando interfacce grandi e monolitiche.
Servizi come TransactionService, BudgetService e SyncService definiscono ciascuno set focalizzati di operazioni.

**Principio di Inversione delle Dipendenze (DIP)**
I moduli di alto livello non dipendono dai moduli di basso livello; entrambi dipendono da astrazioni.
I controller dipendono da interfacce di servizio piuttosto che da implementazioni concrete, permettendo facile testing ed estensione.

**Stile del Codice e Modularità**
Il codice segue convenzioni di naming e formattazione consistenti per la leggibilità.
Il progetto è organizzato in package e livelli logici (controller, servizi, modelli, repository).
Classi di utilità sono utilizzate per task comuni (es. formattazione date e valute), riducendo la duplicazione del codice.

**Efficienza e Manutenibilità**
L'accesso ai dati e l'elaborazione efficienti sono garantiti attraverso l'uso di JPA/Hibernate e query ottimizzate.
Il design modulare e la chiara separazione delle responsabilità rendono il codice facile da mantenere ed estendere.
Questo impegno per i principi SOLID e la qualità del codice garantisce che la Family Budget App sia robusta, adattabile e pronta per la crescita futura.

## Strumenti e Metodologie
Lo sviluppo della Family Budget App sfrutta una gamma di strumenti moderni e metodologie di ingegneria del software per garantire un'applicazione robusta, manutenibile e user-friendly. I principali strumenti e metodologie utilizzati includono:

**Linguaggio di Programmazione e Framework**
Java: Il linguaggio di programmazione primario per tutta la logica dell'applicazione.
JavaFX: Utilizzato per costruire l'interfaccia grafica utente, fornendo un'esperienza utente moderna e reattiva.

**Build e Gestione delle Dipendenze**
Gradle: Utilizzato come strumento di automazione del build, gestendo dipendenze, compilando codice e impacchettando l'applicazione per la distribuzione.

**Persistenza e Gestione dei Dati**
JPA (Java Persistence API): Utilizzato per object-relational mapping e interazioni con il database.
Hibernate: L'implementazione JPA scelta per persistenza dei dati efficiente e affidabile.

**Architettura e Pattern di Design**
MVC (Model-View-Controller): L'applicazione segue il pattern MVC, separando le responsabilità tra dati (modelli), logica di business (servizi) e interfaccia utente (controller/viste).
Pattern Service e Factory: I servizi incapsulano la logica di business e il pattern factory è utilizzato per l'istanziazione e gestione dei servizi.

**Controllo Versione**
Git: Utilizzato per la gestione del codice sorgente, abilitando sviluppo collaborativo e tracking delle versioni.

**Altre Utility**
Classi di Utilità: Classi di utilità personalizzate sono utilizzate per task come formattazione date, formattazione valute e calcoli statistici.

**Metodologie di Sviluppo**
Design Modulare: Il codice è organizzato in moduli e package logici per chiarezza e manutenibilità.
Principi SOLID: Il codice aderisce ai principi SOLID per garantire estensibilità e qualità del codice.
Sviluppo Iterativo: Le funzionalità sono sviluppate e testate incrementalmente, permettendo miglioramento continuo e integrazione del feedback.

Questi strumenti e metodologie contribuiscono collettivamente all'affidabilità, manutenibilità ed estensibilità della Family Budget App.

## Conclusione
La Family Budget App fornisce con successo una soluzione completa per la gestione delle finanze domestiche, combinando funzionalità robuste con un'interfaccia user-friendly. Attraverso funzionalità come gestione delle transazioni e del budget, transazioni programmate e ricorrenti, tracciamento delle scadenze, statistiche avanzate e ricerca e filtri flessibili, l'applicazione permette agli utenti di prendere il controllo della loro pianificazione finanziaria e spese quotidiane.

L'architettura del progetto è progettata per estensibilità e manutenibilità, seguendo i principi SOLID e sfruttando strumenti e pattern di sviluppo moderni. L'uso di JavaFX per l'UI, JPA/Hibernate per la persistenza e una struttura modulare orientata ai servizi garantisce che l'applicazione sia sia affidabile che pronta per miglioramenti futuri.

Mentre la versione attuale fornisce tutte le funzionalità core richieste per una gestione efficace del budget familiare, il design permette l'integrazione seamless di nuove funzionalità, come sincronizzazione cloud, interfacce mobili o web e analisi avanzate. Il meccanismo di sincronizzazione basato su file già pone le basi per il supporto multi-dispositivo, e la chiara separazione delle responsabilità rende facile estendere o adattare l'applicazione mentre le esigenze degli utenti evolvono.
In sintesi, la Family Budget App è una base solida per lo sviluppo continuo e l'uso nel mondo reale, offrendo sia valore immediato che flessibilità a lungo termine per le famiglie che cercano di migliorare il proprio benessere finanziario.

## Screenshot (Opzionale ma Raccomandato)
Includere screenshot delle principali schermate UI per illustrare le funzionalità.

---

## Appendice (Opzionale)
- Note aggiuntive, diagrammi o riferimenti. 
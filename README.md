flowchart LR
    Start([🚀 Start: Fetching DC])
    
    Start --> Fetch[📱 Fetch Current<br/>Connected Cell Tower]
    
    Fetch --> TM{📡 Telephony<br/>Manager}
    
    TM -->|Local DB Query| Search[(🔍 Search in<br/>Local NeoDB)]
    
    Search --> LatLong[📍 Get Coordinates<br/>lat, long]
    
    LatLong --> Find[⚙️ Find Algorithm]
    
    Find --> NS[➡️ Next Station]
    Find --> SC[📊 Stations Covered]
    Find --> Dist[📏 Distance]
    
    LatLong --> State{🎯 State<br/>Detection}
    
    State --> App[🟢 Approaching]
    State --> Near[🟡 Nearby]
    State --> Dep[🔴 Departing]
    
    TM -->|API Request| FetchStops[📥 Fetch stops.json<br/>towers.geojson]
    
    FetchStops --> Server[🖥️ Node Backend<br/>Server]
    
    Server --> MongoDB[(💾 MongoDB<br/>Remote Database)]
    
    MongoDB --> Railway[☁️ Hosted on<br/>Railway]
    
    style Start fill:#4CAF50,stroke:#2E7D32,stroke-width:3px,color:#fff
    style Fetch fill:#2196F3,stroke:#1565C0,stroke-width:2px,color:#fff
    style TM fill:#FF9800,stroke:#E65100,stroke-width:3px,color:#fff
    style Search fill:#9C27B0,stroke:#6A1B9A,stroke-width:2px,color:#fff
    style Find fill:#00BCD4,stroke:#006064,stroke-width:2px,color:#fff
    style State fill:#FF9800,stroke:#E65100,stroke-width:3px,color:#fff
    style Server fill:#F44336,stroke:#C62828,stroke-width:2px,color:#fff
    style MongoDB fill:#607D8B,stroke:#37474F,stroke-width:2px,color:#fff
    style Railway fill:#795548,stroke:#4E342E,stroke-width:2px,color:#fff

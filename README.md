# Zugsuche
Wer kennt nicht die Situation, in der man an einem Bahngleis vorbeigeht/vorbeifährt und sich fragt, "Wann würde hier wohl welcher Zug durchfahren?".
Diese App versucht in solchen Situationen ein paar Informationen zu berechnen.  
Die Bibliothek die von der App verwendet wird ist hier zu finden: https://github.com/FreDeko06/TrainSearch

## Probleme
Bei sehr stark ausgelasten Strecken mit vielen und langen Routen kann es teils sehr lang dauern, bis die Ergebnisse berechnet wurden.  
Es werden nur die Daten in Deutschland ausgewertet.

## Woher stammen die Daten
Zum einen werden Kartendaten von [OpenStreetMap](https://www.openstreetmap.de/) genutzt um die Schiene am jetzigen Standort zu finden, sowie welche Linien diese Schiene nutzen.  
Weiterhin werden dann auf Grundlage der Zeitpläne von [GTFS](https://gtfs.de/de/feeds/) die Zeiten berechnet, in denen ein Zug vorbeifahren wird.


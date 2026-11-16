#!/bin/sh
# Construye y ejecuta el proyecto sin Maven. Requiere un JDK 17 o superior.
# Uso: ./antdron.sh [ejecutar | consola | probar | xp ... | compilar | limpiar]
cd "$(dirname "$0")" || exit 2
command -v java >/dev/null 2>&1 || { echo 'ERROR: no se encontró "java". Instale un JDK 17 o superior.'; exit 2; }
exec java herramientas/Construir.java "$@"

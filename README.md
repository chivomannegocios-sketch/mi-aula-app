# Mi Aula (app Android)

App que muestra Mi Aula sin internet. El progreso y las estadísticas se guardan en el celular.

## Cómo sacar el APK con GitHub
1. Crea un repositorio nuevo en GitHub (por ejemplo `mi-aula-app`).
2. Sube TODO el contenido de esta carpeta (incluida la carpeta oculta `.github`).
3. Entra a la pestaña **Actions** → "Construir APK". Se ejecuta solo al subir; espera unos 3–5 minutos.
4. Abre la ejecución terminada (✓ verde) y descarga **MiAula-apk** en la sección *Artifacts*.
5. Descomprime el zip y abre `app-debug.apk` en el celular para instalarlo.

## Actualizar el contenido
Reemplaza `app/src/main/assets/index.html` por la versión nueva y sube `versionCode` en `app/build.gradle`.

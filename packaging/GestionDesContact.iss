
#define MyAppName "GestionDesContact"
#define MyAppVersion "1.0.0"
#define MyAppExeName "GestionDesContact.exe"

[Setup]
AppId={{A82F1C73-9B42-4D61-A8E5-2F7C6D91B430}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName=C:\GestionDesContact
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
OutputDir=C:\Users\Razafindrakoto\IdeaProjects\GestionDesContact\packaging\installer
OutputBaseFilename=Setup_GestionDesContact
SetupIconFile=C:\Users\Razafindrakoto\IdeaProjects\GestionDesContact\src\main\resources\images\logo.ico
UninstallDisplayIcon={app}\GestionDesContact.exe
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Tasks]
Name: "desktopicon"; Description: "Créer un raccourci sur le Bureau"; GroupDescription: "Raccourcis supplémentaires :"; Flags: unchecked

[Files]
Source: "C:\Users\Razafindrakoto\IdeaProjects\GestionDesContact\packaging\output\GestionDesContact\*"; DestDir: "{app}"; Excludes: "db.properties"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "C:\Users\Razafindrakoto\IdeaProjects\GestionDesContact\packaging\output\GestionDesContact\db.properties"; DestDir: "{app}"; Flags: onlyifdoesntexist

[Icons]
Name: "{group}\GestionDesContact"; Filename: "{app}\GestionDesContact.exe"; WorkingDir: "{app}"
Name: "{autodesktop}\GestionDesContact"; Filename: "{app}\GestionDesContact.exe"; WorkingDir: "{app}"; Tasks: desktopicon


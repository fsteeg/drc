;Change this file to customize zip2exe generated installers with a modern interface

;This file should go into the NSIS\Contrib\zip2exe\ folder, use with a Tycho-built zip
;Our setup in Zip2Exe: load Tycho zip, installer name: DRC, default folder: $PROGRAMFILES\DRC

!include "MUI.nsh"

;Request application privileges for Windows Vista
RequestExecutionLevel user

Var StartMenuFolder

!insertmacro MUI_PAGE_DIRECTORY
;Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "DRC"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN "$INSTDIR\eclipse.exe"
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Install" SecDummy
SetOutPath "$INSTDIR"
;Create uninstaller
WriteUninstaller "$INSTDIR\Uninstall.exe"
!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
CreateShortCut "$SMPROGRAMS\$StartMenuFolder\DRC.lnk" "$INSTDIR\eclipse.exe"
CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
!insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section "Uninstall"
Delete "$INSTDIR\Uninstall.exe"
RMDir /r "$INSTDIR"
!insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder
Delete "$SMPROGRAMS\$StartMenuFolder\DRC.lnk"
Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
RMDir "$SMPROGRAMS\$StartMenuFolder"
DeleteRegKey /ifempty HKCU "DRC"
SectionEnd

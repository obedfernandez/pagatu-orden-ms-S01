# Dot-source este archivo antes de llamar Get-PagatuKeycloakToken.
# El login ocurre en el navegador con PKCE S256; sin password grant.
function Get-PagatuKeycloakToken {
    param([string]$Cuenta = 'CLIENTE')
    $ErrorActionPreference = 'Stop'
    $issuer = 'http://localhost:18180/realms/pagatu'
    $callback = 'http://localhost:4200/auth/callback'
    $random = New-Object byte[] 48
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($random)
    $verifier = [Convert]::ToBase64String($random).TrimEnd('=').Replace('+','-').Replace('/','_')
    $hash = [System.Security.Cryptography.SHA256]::Create().ComputeHash([Text.Encoding]::ASCII.GetBytes($verifier))
    $challenge = [Convert]::ToBase64String($hash).TrimEnd('=').Replace('+','-').Replace('/','_')
    $stateBytes = New-Object byte[] 16
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($stateBytes)
    $state = [Convert]::ToBase64String($stateBytes).TrimEnd('=').Replace('+','-').Replace('/','_')
    $url = "$issuer/protocol/openid-connect/auth?client_id=pagatu-frontend&response_type=code&redirect_uri=$([uri]::EscapeDataString($callback))&scope=openid%20profile&code_challenge=$challenge&code_challenge_method=S256&state=$state"
    $url += '&prompt=login'

    if ($Cuenta -eq 'ADMIN') {
        $url += '&login_hint=admin'
    } else {
        $url += '&login_hint=cliente1'
    }
    Write-Host "Acceso Keycloak ($Cuenta): abriendo navegador..." -ForegroundColor Cyan
    Start-Process $url
    Write-Host 'Inicia sesion. El error de localhost:4200 es normal si no existe Angular.'
    $redirect = Read-Host 'Pega aqui TODA la URL de redireccion del navegador'
    if ($redirect -notmatch '[?&]code=([^&#]+)') { throw 'No se encontro code en la URL de retorno.' }
    $code = [uri]::UnescapeDataString($Matches[1])
    if ($redirect -notmatch '[?&]state=([^&#]+)') { throw 'No se encontro state en la URL.' }
    $receivedState = [uri]::UnescapeDataString($Matches[1])
    if ($receivedState -cne $state) { throw 'State no coincide: autenticacion cancelada.' }
    $tokenResponse = Invoke-RestMethod -Method Post -Uri "$issuer/protocol/openid-connect/token" `
        -Body @{ grant_type='authorization_code'; client_id='pagatu-frontend'; redirect_uri=$callback; code=$code; code_verifier=$verifier }
    if (-not $tokenResponse.access_token) { throw 'Keycloak no devolvio access_token.' }
    return $tokenResponse.access_token
}

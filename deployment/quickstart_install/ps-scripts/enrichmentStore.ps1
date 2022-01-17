
Write-Output "********* Set up demo enrichment table **************"
Write-Output "*****************************************************"

# Create table file
$FileName = "hostname.json"
Write-Output '{"1.2.3.4":{"hostname":"test-name"}}' > $FileName

# POST request
$FilePath = (Get-Location).path + '/' + $FileName
$URL = 'https://enrichment.siembol.local/upload.php';
$boundary = [System.Guid]::NewGuid().ToString(); 
$LF = "`r`n";

$bodyLines = ( 
    "--$boundary",
    "Content-Disposition: form-data; name=`"uploaded_file`"; filename=`"$FileName`"",
    "Content-Type: multipart/form-data$LF",
    (Get-content $filepath),
    "--$boundary--$LF" 
) -join $LF

Invoke-RestMethod -Uri $URL -Method Post -ContentType "multipart/form-data; boundary=`"$boundary`"" -Body $bodyLines

# Call siembol rest endpoint to sync
$header=@{"content-type"="application/json"}
$restUri="https://rest.siembol.local/api/v1/enrichment/enrichment/tables"
$body=@{"name"= "hostname";"path"= "/download.php?filename=$FileName"}
Invoke-RestMethod -Method 'Post' -Uri $restUri -Headers $header -Body ($body|ConvertTo-Json)


Write-Output "************************************************************"
Write-Output "Check uploaded table through this url in the browser:"
Write-Output "https://enrichment.siembol.local/download.php?filename=$FileName"
Write-Output "************************************************************"
Write-Output "Check siembol table info through this url in the browser:"
Write-Output "https://rest.siembol.local/api/v1/enrichment/enrichment/tables"
Write-Output "************************************************************"
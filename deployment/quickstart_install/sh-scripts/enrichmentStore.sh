echo "********* Set up demo enrichment table **************"
echo "*****************************************************"

echo '{"1.2.3.4":{"hostname":"test.com"}}' > hostname.json
curl -F "uploaded_file=@hostname.json;" https://enrichment.local/upload.php

curl -X 'POST' https://rest.siembol.local/api/v1/enrichment/enrichment/tables -H 'accept: application/json' -H 'Content-Type: application/json' -d '{\"name\": \"hostname\",\"path\": \"/download.php?filename=dns.json\"}'

echo "************************************************************"
echo "Check uploaded table through this url in the browser:"
echo "https://enrichment.local/download.php?filename=hostname.json"
echo "************************************************************"
echo "Check siembol table info through this url in the browser:"
echo "https://rest.siembol.local/api/v1/enrichment/enrichment/tables"
echo "************************************************************"
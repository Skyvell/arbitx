goloop rpc sendtx deploy ./balanced-liquidation/build/libs/balanced-liquidation-0.1.0-optimized.jar \
    --uri http://localhost:9082/api/v3 \
    --key_store ./godWallet.json --key_password gochain \
    --nid 3 --step_limit 3000000000 \
    --content_type application/java \
    --param name=contract_34
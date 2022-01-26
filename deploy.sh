goloop rpc sendtx deploy ./liquidation_strategies/build/libs/liquidation_strategies-0.1.0-optimized.jar \
    --uri https://berlin.net.solidwallet.io/api/v3 \
    --key_store ./arbitx.json --key_password test \
    --nid 0x7 --step_limit 3000000000 \
    --content_type application/java \
    --param name=contract_34
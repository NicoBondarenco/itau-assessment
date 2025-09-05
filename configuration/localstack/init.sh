#!/bin/bash

set -e

echo "Starting AWS resources initialization..."
echo "$(date): LocalStack init script started"

wait_for_localstack() {
    echo "Waiting for LocalStack to be fully ready..."
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:4566/_localstack/health" > /dev/null 2>&1; then
            local health_status=$(curl -s "http://localhost:4566/_localstack/health" | grep -o '"sqs": "[^"]*"' | cut -d'"' -f4)
            if [ "$health_status" = "available" ]; then
                echo "LocalStack is ready (attempt $attempt/$max_attempts)"
                return 0
            fi
        fi
        echo "⏳ Waiting for LocalStack... (attempt $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    echo "LocalStack failed to become ready after $max_attempts attempts"
    exit 1
}

create_sqs_resources() {
    echo "Creating SQS resources..."

    echo "Creating account-transaction-dlq..."
    aws --endpoint-url=http://localhost:4566 sqs create-queue \
        --queue-name account-transaction-dlq.fifo \
        --attributes '{
            "MessageRetentionPeriod": "1800",
            "FifoQueue": "true",
            "ContentBasedDeduplication": "true"
        }' --region sa-east-1

    local dlq_url=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
        --queue-name account-transaction-dlq --output text --query 'QueueUrl' --region sa-east-1)
    local dlq_arn=$(aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes \
        --queue-url "$dlq_url" --attribute-names QueueArn --output text \
        --query 'Attributes.QueueArn' --region sa-east-1)

    echo "DLQ created with ARN: $dlq_arn"

    echo "Creating account-transaction with DLQ configuration..."
    aws --endpoint-url=http://localhost:4566 sqs create-queue \
        --queue-name account-transaction.fifo \
        --attributes '{
            "MessageRetentionPeriod": "1800",
            "VisibilityTimeout": "60",
            "ReceiveMessageWaitTimeSeconds": "5",
            "FifoQueue": "true",
            "ContentBasedDeduplication": "true",
            "RedrivePolicy": "{\"deadLetterTargetArn\":\"$dlq_arn\",\"maxReceiveCount\":3}"
        }' --region sa-east-1

    echo "SQS resources created successfully!"
}

create_dynamodb_resources() {
    echo -e "Creating table 'account'..."

    aws dynamodb create-table \
        --endpoint-url="http://localhost:4566" \
        --region="sa-east-1" \
        --table-name="account" \
        --billing-mode PAY_PER_REQUEST \
        --attribute-definitions \
            AttributeName=accountId,AttributeType=S \
            AttributeName=createdAt,AttributeType=S \
        --key-schema \
            AttributeName=accountId,KeyType=HASH \
            AttributeName=createdAt,KeyType=RANGE \

    echo -e "Creating table 'balance'..."

    aws dynamodb create-table \
        --endpoint-url="http://localhost:4566" \
        --region="sa-east-1" \
        --table-name="balance" \
        --billing-mode PAY_PER_REQUEST \
        --attribute-definitions \
            AttributeName=accountId,AttributeType=S \
        --key-schema \
            AttributeName=accountId,KeyType=HASH \

    echo -e "Creating table 'transaction'..."

    aws dynamodb create-table \
        --endpoint-url="http://localhost:4566" \
        --region="sa-east-1" \
        --table-name="transaction" \
        --billing-mode PAY_PER_REQUEST \
        --attribute-definitions \
            AttributeName=transactionId,AttributeType=S \
            AttributeName=accountId,AttributeType=S \
            AttributeName=timestamp,AttributeType=S \
        --key-schema \
            AttributeName=transactionId,KeyType=HASH \
            AttributeName=timestamp,KeyType=RANGE \
        --global-secondary-indexes '[
            {
                "IndexName": "AccountIdIndex",
                "KeySchema": [
                    {
                        "AttributeName": "accountId",
                        "KeyType": "HASH"
                    },
                    {
                        "AttributeName": "timestamp",
                        "KeyType": "RANGE"
                    }
                ],
                "Projection": {
                    "ProjectionType": "ALL"
                }
            }
        ]'

    echo "DynamoDB resources created successfully!"
}

display_resources() {
    echo "=== CREATED RESOURCES ==="

    echo "SQS Queues:"
    aws --endpoint-url=http://localhost:4566 sqs list-queues --region sa-east-1

    echo "DynamoDB Tables:"
    aws --endpoint-url=http://localhost:4566 dynamodb list-tables --region sa-east-1

    echo "Access URLs:"
    echo "- LocalStack Endpoint: http://localhost:4566"
    echo "- Health Check: http://localhost:4566/_localstack/health"
}

generate_uuid() {
    if command -v uuidgen &> /dev/null; then
        uuidgen | tr '[:upper:]' '[:lower:]'
    else
        # Fallback para sistemas sem uuidgen
        python3 -c "import uuid; print(str(uuid.uuid4()))"
    fi
}

generate_zoned_datetime() {
    if command -v gdate &> /dev/null; then
        # Para macOS com GNU date
        gdate -u +"%Y-%m-%dT%H:%M:%S.%6N%z" | sed 's/+0000/Z/'
    else
        # Para Linux
        date -u +"%Y-%m-%dT%H:%M:%S.%6NZ"
    fi
}

generate_daily_limit() {
    local min=1000
    local max=10000
    local step=100

    local range=$(( (max - min) / step ))
    local random_step=$(( RANDOM % (range + 1) ))
    local limit=$(( min + (random_step * step) ))

    echo "$limit.00"
}

generate_balance() {
    local min=15000
    local max=50000
    local step=1000

    local range=$(( (max - min) / step ))
    local random_step=$(( RANDOM % (range + 1) ))
    local limit=$(( min + (random_step * step) ))

    echo "$limit.00"
}

generate_amount() {
    local min=100
    local max=500
    local step=10

    local range=$(( (max - min) / step ))
    local random_step=$(( RANDOM % (range + 1) ))
    local limit=$(( min + (random_step * step) ))

    echo "$limit.00"
}

generate_quantity() {
    local min=0
    local max=10
    local step=1

    local range=$(( (max - min) / step ))
    local random_step=$(( RANDOM % (range + 1) ))
    local limit=$(( min + (random_step * step) ))

    echo $limit
}

generate_random_boolean() {
    local false_percentage=10
    local random_value=$(( RANDOM % 100 ))

    if [ "$random_value" -lt "$false_percentage" ]; then
        echo "false"
    else
        echo "true"
    fi
}

main() {
    echo "Executing LocalStack initialization..."

    wait_for_localstack

    create_sqs_resources

    create_dynamodb_resources

    display_resources

    echo "LocalStack initialization completed successfully!"
    echo "$(date): Initialization finished"
}

main "$@"

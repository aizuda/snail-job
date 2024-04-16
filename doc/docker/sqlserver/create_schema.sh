#!/usr/bin/env bash

/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'SnailJob@24' -Q "CREATE DATABASE snail_job;
GO"
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'SnailJob@24' -d snail_job -i /tmp/schema.sql

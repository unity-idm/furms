#!/usr/bin/env bash
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
# See LICENSE file for licensing information.

typeset -r RUNNER_HOME=../../../python-agent
typeset -r CA_FILE_PATH=${RUNNER_HOME}/devrunner/ca_certificate.pem
typeset -r RUNNER=${RUNNER_HOME}/devrunner/agent.py
typeset -r CMD_FOLDER=/tmp/agetns-dev

function demonizeSingleAgent()
{
	typeset QUEUE_NAME=$1
	export CA_FILE=${CA_FILE_PATH}
	export PYTHONPATH=${RUNNER_HOME}
	nohup /usr/bin/python3 ${RUNNER} $QUEUE_NAME > ${CMD_FOLDER}/${QUEUE_NAME}.log 2>&1 &
	echo $! > ${CMD_FOLDER}/${QUEUE_NAME}.pid
}

function stopSingleAgent()
{
	typeset QUEUE_NAME=$1
	typeset PID=$(cat ${CMD_FOLDER}/${QUEUE_NAME}.pid)
	kill ${PID}
}

function startAgents()
{
	demonizeSingleAgent "bsc-x"
	demonizeSingleAgent "fzj-x"
	demonizeSingleAgent "cin-x"
}

function stopAgents()
{
	stopSingleAgent "bsc-x"
	stopSingleAgent "fzj-x"
	stopSingleAgent "cin-x"
}

function printUsageAndExit()
{
	echo "Usage: $0 start|stop"
	exit 1
}

mkdir -p ${CMD_FOLDER}

if [[ $# -ne 1 ]]
then
	printUsageAndExit
fi

if [[ $1 == "start" ]]
then
	startAgents
elif [[ $1 == "stop" ]]
then
	stopAgents
else
	printUsageAndExit
fi
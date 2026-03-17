#!/bin/bash -e

set -o pipefail || true  # trace ERR through pipes
set -o errtrace || true # trace ERR through commands and functions
set -o errexit || true  # exit the script if any statement returns a non-true return value

on_error(){
  echo "ERROR: command: ${BASH_COMMAND}"
}
trap on_error ERR

usage(){
  cat <<EOF

DESCRIPTION: Slack notifier.

USAGE:

$(basename "${SCRIPT_PATH}") [OPTIONS] --app=APP --workspace=WORKSPACE --channel=CHANNEL --token=TOKEN --color=COLOR MESSAGE

  --color=COLOR
        Any of good, warning, danger, or any hex color code (eg. #439FE0).
        Default: warning

  --app=APP
        Slack application, E.g. 'jenkins-ci'.
        Can be passed using SLACK_APP environment variable.

  --workspace=WORKSPACE
        Slack workspace name.
        E.g. If you sign in to slack at https://example.slack.com/, your workspace is 'example'.
        Can be passed using SLACK_WORKSPACE environment variable.

  --channel=CHANNEL
        Slack channel name (without leading #)
        Can be passed using SLACK_CHANNEL environment variable.

  --token=TOKEN
        Slack access token.
        Can be passed using SLACK_TOKEN environment variable.

  MESSAGE text of the message to send

OPTIONS:

  --help
          Prints the usage and exits.

  --q
          Remove stderr output.

  --v
          Add traces to stderr output.

EOF
}

# parse command line args
ARGS=( "${@}" )
for ((i=0;i<${#ARGS[@]};i++))
{
  ARG=${ARGS[${i}]}
  if [ ${#ARG} -eq 0 ] ; then continue; fi
  case ${ARG} in
    "--color="*)
      readonly SLACK_MESSAGE_COLOR="${ARG#*=}"
      ;;
    "--app="*)
      SLACK_APP="${ARG#*=}"
      ;;
    "--workspace="*)
      SLACK_WORKSPACE="${ARG#*=}"
      ;;
    "--channel="*)
      SLACK_CHANNEL="${ARG#*=}"
      ;;
    "--token="*)
      SLACK_TOKEN="${ARG#*=}"
      ;;
    "--help"|"-help"|"-h"|"--h")
      usage
      exit 0
      ;;
    "--q"|"-q")
      readonly STDERR_LOG=$(mktemp -t XXXstderr)
      echo "INFO: redirecting stderr to ${STDERR_LOG}" >&2
      exec 2> "${STDERR_LOG}"
      ;;
    "--v"|"-v")
      set -x
      ;;
    "-"*)
      echo "ERROR: unknown option: ${ARG}"
      exit 1
      ;;
    *)
      readonly SLACK_MESSAGE_TEXT="${ARG}"
      ;;
  esac
}

if [ -z "${SLACK_APP}" ] ; then
  echo "ERROR: --app is required"
  exit 1
elif [ -z "${SLACK_WORKSPACE}" ] ; then
  echo "ERROR: --workspace option is required"
  exit 1
elif [ -z "${SLACK_CHANNEL}" ] ; then
  echo "ERROR: --channel option is required"
  exit 1
elif [ -z "${SLACK_TOKEN}" ] ; then
  echo "ERROR: --token option is required"
  exit 1
elif [ -z "${SLACK_MESSAGE_TEXT}" ] ; then
  echo "ERROR: message is required"
  exit 1
fi

if [ -z "${SLACK_MESSAGE_COLOR}" ] ; then
  SLACK_MESSAGE_COLOR="warning"
fi

readonly SLACK_URL="https://${SLACK_WORKSPACE}.slack.com/services/hooks/${SLACK_APP}?token=${SLACK_TOKEN}"

curl "${SLACK_URL}" -d "
{
  \"channel\": \"${SLACK_CHANNEL}\",
  \"attachments\": [
    {
      \"color\": \"${SLACK_MESSAGE_COLOR}\",
      \"fallback\": \"${SLACK_MESSAGE_TEXT}\",
      \"fields\": [
        {
          \"short\": false,
          \"value\": \"${SLACK_MESSAGE_TEXT}\"
        }
      ],
      \"mrkdwn_in\": [
        \"pretext\",
        \"text\",
        \"fields\"
      ]
    }
  ],
  \"link_names\": \"1\",
  \"unfurl_links\": \"true\",
  \"unfurl_media\": \"true\"
}
"

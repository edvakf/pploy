# pploy

The deploy lock manager for [pixiv](http://www.pixiv.net/).

With pploy you can;

* deploy any git repository with a deploy script placed at .deploy/bin/deploy
* add a git repository for deploy list easily
* let your project members lock a project between staging check and production deploy
* configure LDAP info to fetch staffs' names
* notify deployments to [Slack](https://slack.com/) and [idobata](https://idobata.io/)

## Requirements to a repository to be deployed

It must be a git repo.

It must have an executable file `.deploy/bin/deploy` at the top level of the repo.

It can optionally have `.deploy/config/readme.html` which will be displayed at it's deploy page.

The deploy script is run as;

```
DEPLOY_USER=foo DEPLOY_ENV=staging .deploy/bin/deploy
```

So you can do pretty much anything there, eg. call a Capistrano command.

### Changing the `DEPLOY_ENV`s

By default, `staging` and `production` are the only deploy envs supported. If you want to change them, add a file named `.deploy/config/deploy_envs` in the repo with one line per env.

```
preview
staging
production
```

## Config values

### Working directory

```
pploy.dir="/tmp/pploy"
```

The working directory in which repos are cloned and logs are written.

### Deploy user names

```
pploy.users=["foo", "bar"]
```

Names of users who can gain lock of projects.

### LDAP

```
pploy.ldap.url="ldap://ldap.example.com:389"
pploy.ldap.login="cn=someone,dc=example,dc=com"
pploy.ldap.password="SomeonesPassword"
pploy.ldap.search="dc=deployers,dc=example,dc=com"
pploy.ldap.cachettl=3600
```

You can optionally set an LDAP configuration from which to fetch member names.

LDAP's `cn` (Common Name) values are used.

If `pploy.ldap.cachettl` is omitted, it caches the names for an hour.

### Lock minutes

```
pploy.lock.gainMinutes=20
pploy.lock.extendMinutes=10
```

These are for how long a project is locked once someone gained and extended a lock.

### Slack

```
pploy.slack.endpoint="https://hooks.slack.com/services/XXXXXXXXX/XXXXXXXXX/1234567890abcdefghijklmn"
```

If enabled, deploy logs are posted to Slack through an Incoming WebHook integration.

### Idobata

```
pploy.idobata.endpoint="https://idobata.io/hook/generic/11112222-3333-4444-5555-666677778888"
```

If enabled, deploy logs are posted to idobata.

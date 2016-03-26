Flamegraph Plugin
=================

perf without password
---------------------
Since `perf` requires sudo to be actually useful, you may want to make it sudo-able
without you having to type the password (when used by tools, such as this plugin).
It's pretty simple to setup sudoers to allow access to `/ust/bin/perf` without
having to type your password all the time, simply add the following to the sudoers
file (use `sudo visudo`):

```
USERNAME ALL = NOPASSWD: /usr/bin/perf
```

Make sure that you provide the full path to `/usr/bin/perf`, not just `perf` -
as this would allow executing any binary which is named `perf`, whereas you mean the
`/usr/bin/perf` specifically.

Also do:

```
echo 0 > /proc/sys/kernel/kptr_restrict
```
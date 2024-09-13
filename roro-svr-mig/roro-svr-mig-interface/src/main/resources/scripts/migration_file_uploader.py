# -*- coding: utf-8 -*-
import argparse
import subprocess


class FileUploader(object):
    '''
    Do migration tasks
    '''

    @classmethod
    def get_args(cls):
        '''
        Parses arguments and return
        
        :return: 
        '''

        # Assign description to the help doc
        parser = argparse.ArgumentParser(
            formatter_class=argparse.RawDescriptionHelpFormatter,
            description='RoRo Replatform Migration FileUploader parser')

        # Source server info
        parser.add_argument('-H', '--host', type=str, help='source host ip', required=True)
        parser.add_argument('-P', '--port', type=int, default=22, help='ssh port', required=False)
        parser.add_argument('-u', '--username', type=str, help='username', required=True)
        parser.add_argument('-p', '--password', type=str, help='password', required=False)
        parser.add_argument('-k', '--keyfile', type=str, help='key file path', required=False)
        parser.add_argument('-s', '--sudoer', type=str, default='true', help='Is sudoer - true / false', required=False)

        parser.add_argument('--backup_dir', type=str, help='backup directory', required=True)
        parser.add_argument('--log_dir', type=str, help='log directory', required=True)

        # Array for all arguments passed to script
        args = parser.parse_args()

        return args

    @classmethod
    def parsing_args(cls, args):
        '''
        Change arguments to dictionary and return
        
        :param args: 
        :return: 
        '''

        param = dict()

        param['host'] = args.host
        param['port'] = args.port
        param['username'] = args.username
        param['password'] = args.password or None
        param['keyfile'] = args.keyfile or None
        param['sudoer'] = args.sudoer
        param['backup_dir'] = args.backup_dir
        param['log_dir'] = args.log_dir

        return param

    @classmethod
    def run_subprocess(cls, cmd1):
        '''
        Execute linux system command and logging results
        
        :param cmd1: 
        :param log_enabled: 
        :return: 
        '''

        proc = subprocess.Popen(cmd1, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (out, err) = proc.communicate()

        if out:
            return out
        else:
            return err

    @classmethod
    def make_dirs(cls, args):
        '''
        Make a necessary directory if does not exist

        :param args:
        :return:
        '''

        cmd1 = ['sudo mkdir -p ' + args.log_dir]
        proc = subprocess.Popen(cmd1, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (out, err) = proc.communicate()

        if out:
            return out
        else:
            return err


def main():
    args = FileUploader.get_args()
    param = FileUploader.parsing_args(args)
    FileUploader.make_dirs(args)

    '''
    rsync -av -H -S -e 'ssh -i ~/.ssh/osci-key.pem -p 22 -l ec2-user -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' 
    --rsync-path="/usr/bin/sudo /usr/bin/rsync" --progress ./home2 13.124.185.181:/

    key_file, host, port, username, backup_dir
    
    
    Sudoer & Keyfile 
    /usr/bin/rsync -av -H -S -e 'ssh -i /home/roro/.ssh/osci-key.pem -l wasup -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --rsync-path="/usr/bin/sudo /usr/bin/rsync" --progress --no-owner --no-group /home/roro/linux 192.168.4.10:/tmp/roro/
    
    Keyfile 
    /usr/bin/rsync -av -H -S -e 'ssh -i /home/roro/.ssh/osci-key.pem -l wasup -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --rsync-path="/usr/bin/rsync" --progress --no-owner --no-group /home/roro/linux 192.168.4.10:/tmp/roro/
    
    Sudoer & Password
    /usr/bin/rsync -av -H -S -e 'sshpass -p \'jan01jan\' ssh -l wasup -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --rsync-path="/usr/bin/sudo /usr/bin/rsync" --progress --no-owner --no-group /home/roro/linux 192.168.4.10:/tmp/roro/
    
    Password
    /usr/bin/rsync -av -H -S -e 'sshpass -p \'jan01jan\' ssh -l wasup -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --rsync-path="/usr/bin/rsync" --progress --no-owner --no-group /home/roro/linux 192.168.4.10:/tmp/roro/
    '''

    if param['password']:
        ssh_option = ('sshpass -p "%s" ssh -l %s -p %s -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' % (
            param['password'], param['username'], param['port']))

    if param['keyfile']:
        ssh_option = ('ssh -i %s -l %s -p %s -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' % (
            param['keyfile'], param['username'], param['port']))

    if param['sudoer'] == 'true' and param['username'] != 'root':
        rsync_path = (' --rsync-path="/usr/bin/sudo /usr/bin/rsync"')
    else:
        rsync_path = (' --rsync-path="/usr/bin/rsync"')

    cmd = ['/usr/bin/rsync -av -H -S -e '
           + "'" + ssh_option + "'"
           + rsync_path
           + ' --progress'
           + ' --no-owner'
           + ' --no-group'
           + ' '
           + param['backup_dir']
           + ' '
           + param['host'] + ':/'
           + ' | tee > ' + param['log_dir'] + '/rsync.log']

    print("[File Upload Command]")
    print(cmd)

    result = FileUploader.run_subprocess(cmd1=cmd)
    print(result)


if __name__ == '__main__':
    main()

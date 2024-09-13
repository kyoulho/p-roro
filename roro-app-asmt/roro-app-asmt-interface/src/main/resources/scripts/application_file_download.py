# -*- coding: utf-8 -*-
import argparse
import subprocess


class FileDownloader(object):
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
            description='RoRo Application FileDownloader parser')

        # Source server info
        parser.add_argument('-H', '--host', type=str, help='source host ip', required=True)
        parser.add_argument('-P', '--port', type=int, default=22, help='ssh port', required=False)
        parser.add_argument('-u', '--username', type=str, help='username', required=True)
        parser.add_argument('-p', '--password', type=str, help='password', required=False)
        parser.add_argument('-k', '--keyfile', type=str, help='key file path', required=False)
        parser.add_argument('-s', '--sudoer', type=str, default='true', help='Is sudoer - true / false', required=False)

        parser.add_argument('--source_dir', type=str, help='source directory', required=True)
        parser.add_argument('--target_dir', type=str, help='target directory', required=True)

        parser.add_argument('--parent_dir', type=str, help='target parent directory', required=False)
        parser.add_argument('--asis_dir', type=str, help='target AS-IS directory', required=False)
        parser.add_argument('--tobe_dir', type=str, help='target TO-BE directory', required=False)
        parser.add_argument('--depth', type=int, default=0, help='strip-components', required=False)
        parser.add_argument('--mkdirs', type=str, default='true', help='make directory - true / false', required=False)

        parser.add_argument('--input_file', type=str, help='Get names to create from FILE', required=False)
        parser.add_argument('--exclude', type=str, help='Skip files that match pattern', required=False)

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

        param['source_dir'] = args.source_dir
        param['target_dir'] = args.target_dir
        param['parent_dir'] = args.parent_dir or None
        param['asis_dir'] = args.asis_dir or None
        param['tobe_dir'] = args.tobe_dir or None
        param['depth'] = args.depth
        param['mkdirs'] = args.mkdirs
        param['input_file'] = args.input_file or None
        param['exclude'] = args.exclude or None

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

        cmd1 = ['sudo mkdir -p ' + args.target_dir]
        proc = subprocess.Popen(cmd1, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (out, err) = proc.communicate()

        if out:
            return out
        else:
            return err


def main():
    args = FileDownloader.get_args()
    param = FileDownloader.parsing_args(args)

    if param['mkdirs'] == 'true':
        FileDownloader.make_dirs(args)

    cmd = []

    if param['password']:
        cmd.append('sshpass -p \'' + param['password'] + '\' ')

    cmd.append('ssh ')

    if param['keyfile']:
        cmd.append('-i ' + param['keyfile'] + ' ')

    cmd.append('-p ' + str(param['port']) + ' ')
    cmd.append('-q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ')
    cmd.append(param['username'] + '@' + param['host'] + ' ')

    if param['sudoer'] == 'true' and param['username'] != 'root':
        cmd.append('"sudo tar')
    else:
        cmd.append('"tar')

    if param['exclude']:
        exclude_args = ''
        for arg in param['exclude'].split(','):
            exclude_args += ' --exclude=' + arg
        cmd.append(exclude_args)

    cmd.append('-cf - ')

    if param["input_file"]:
        cmd.append('\\$(cat ' + param["input_file"] + ')')
    else:
        cmd.append(param["source_dir"])

    cmd.append('" |')

    if param['depth'] > 0:
        cmd.append('tar xf - --strip-components=' + str(param['depth']) + ' -C ' + param['target_dir'])
    else:
        cmd.append('tar xf - -C ' + param['target_dir'])

    cmd = [' '.join(cmd)]

    '''
    "tar: Removing leading `/' from member names" 와 같은 Warning Message가 발생하는 경우 아래처럼 grep을 추가하여 표시하지 않을 수 있다.
    sshpass -p xxxx ssh -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null xxxxx@192.168.0.1 "sudo tar -cf - /source_path 2>&1 | grep -v 'Removing leading'" | tar xf - -C /target_path

    sshpass -p xxxx ssh -p 22 -q -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null xxxxx@192.168.0.1 "sudo tar -cf - /source_path" | tar xf - --strip-components=1 -C /target_path
    '''

    # 파일 이동이 필요한 경우 ';' 구분자를 별도의 command로 하여 실행해야 함.
    if param['parent_dir'] and param['asis_dir'] and param['tobe_dir']:
        cmd.append(' ; mkdir -p ' + param['parent_dir'])
        cmd.append(' ; mv -f ' + param['asis_dir'] + ' ' + param['tobe_dir'])
        cmd = [' '.join(cmd)]

    print("[File Download Command]")
    print(cmd)

    result = FileDownloader.run_subprocess(cmd1=cmd)

    print(result)


if __name__ == '__main__':
    main()

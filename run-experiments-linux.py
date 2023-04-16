#!/usr/bin/env python

# run the experiments for all relevant values of k and one specific choice of algorithm

from __future__ import print_function
from subprocess import call

import sys
import random
import os
import argparse
import glob
import subprocess as sp
import multiprocessing as mp

parser = argparse.ArgumentParser(
    description='A script for running our experiments on the real-world and random graphs.')

graph_repo_path = "/export/storage/data"


def work(in_file):
    """Defines the work unit on an input file"""
    # each line in the file contains the following arguments seperated by ;
    # output_file, graph_file, k_start, k_end, stop_after_timeout, algo_type, time_limit, problem_id
    split_line = in_file.split(";")
    # path to the output file relative to where this file is executed
    output_file = split_line[0]
    # path to the graph file relative to where the graph repository is located
    graph_file = graph_repo_path + split_line[1]
    # smallest k for which the algorithm should be run
    k_start = int(split_line[2])
    # largest k for which the algorithm should be run
    k_end = int(split_line[3])
    # if this is true the algorithm should not be executed for bigger k once it has a timeout for a smaller k
    stop_after_timeout = bool(split_line[4])
    # the type of the algorithm that should be used
    algo_type = split_line[5]
    # the time limit for the execution in ms
    time_limit = split_line[6]
    # the problem_id for these instances
    problem_id = int(split_line[7])

    for k in range(k_start, k_end + 1):
        sp.call(
            ["java", "-Djava.library.path=/opt/gurobi1000/linux64/lib/", "-jar", "Code.jar", output_file, graph_file,
             str(k), algo_type, str(problem_id), str(time_limit), "[0]", "0"]
        )
        if stop_after_timeout:
            # now open output_file and check how if the algorithm had a timeout
            time_spent = -1
            for output_line in open(output_file):
                output_split_line = output_line.split(";")
                if int(output_split_line[0]) == problem_id:
                    if output_split_line[8] == "null":
                        time_spent = 3600000
                    else:
                        time_spent = int(output_split_line[8])
                    break
            if time_spent >= 3600000:
                break
        problem_id += 1
    return 0


if __name__ == '__main__':
    files = []
    # experiments for real-world instances
    for line in open(sys.argv[1]):
        if not line.startswith("#"):
            files += [line.strip()]

    # Set up the parallel task pool to use all available processors
    count = 12
    # shuffle such that load on each processor is more evenly distributed
    random.shuffle(files)
    pool = mp.Pool(processes=count)

    # Run the jobs

    pool.map(work, files)

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

graph_repo_path = "./datasets/" #/export/storage/data"
java_class_path = "/home/felix/Documents/weighted_vertex_cover/out/" # /export/storage/ba-felix/out/"


def work(config):
    """Defines the work unit on an input file"""

    # call_args = ["java", "-cp" "/export/storage/ba-felix/out/", "Main"].append(config.split(";"))
    config = config.split(" ")
    in_file_name = graph_repo_path + config[0]
    out_file_name = "./benchmarks_java/" + "_".join(config) + ".out"

    config.pop(0)
    config.insert(0, in_file_name)

    print(out_file_name)

    out_file = open(out_file_name, "w")
    call_args = ["java", "-cp", java_class_path, "Main", *config]
    print(call_args)

    sp.call(
        call_args, stdout=out_file
    )
    return 0


if __name__ == '__main__':
    instances = []
    # experiments for real-world instances
    for line in open(sys.argv[1]):
        if not line.startswith("#"):
            instances += [line.strip()]

    # Set up the parallel task pool to use all available processors
    count = 12 # TODO change to 12
    # shuffle such that load on each processor is more evenly distributed
    random.shuffle(instances)
    pool = mp.Pool(processes=count)

    # Run the jobs

    pool.map(work, instances)

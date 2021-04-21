from setuptools import find_packages, setup

setup(
    name='furms',
    packages=find_packages(include=['furms']),
    version='1.0.0',
    description='FURMS protocol MOM API',
    author='Bixbit s.c.',
    license='BSD 2-Clause',
    install_requires=['pika']
)